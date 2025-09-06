package com.goormthon.careroad.jobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goormthon.careroad.common.BusinessException;
import com.goormthon.careroad.common.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class JobService {

    final JobRepository repo;
    final ObjectMapper om;

    public JobService(JobRepository repo, ObjectMapper om) {
        this.repo = repo;
        this.om = om;
    }

    public Page<Job> list(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Job get(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Job not found: " + id));
    }

    /** 컨트롤러에서 사용하는 생성 진입점 */
    @Transactional
    public Job create(JobCreateRequest body) {
        Job j = Job.builder()
                .name(body.name)
                .type(body.type)
                .request(body.request)
                .status(JobStatus.QUEUED)
                .build();
        return repo.save(j);
    }

    /** 직접 타입/리퀘스트로 큐잉하고 싶을 때 사용하는 API */
    @Transactional
    public Job enqueue(String type, String request) {
        Job j = Job.builder()
                .type(type)
                .request(request)
                .status(JobStatus.QUEUED)
                .build();
        return repo.save(j);
    }

    /** 아주 단순한 데모 실행 로직 */
    @Transactional
    public Job runOnce(UUID id) {
        Job j = get(id);
        try {
            j.setStatus(JobStatus.RUNNING);
            repo.save(j);

            String result = toJson(Map.of("ok", true, "echo", j.getRequest()));
            j.setResult(result);
            j.setStatus(JobStatus.SUCCEEDED);
            return repo.save(j);

        } catch (Exception ex) {
            j.setStatus(JobStatus.FAILED);
            try {
                j.setResult(toJson(Map.of("error", ex.getMessage())));
            } catch (Exception ignore) {
                j.setResult("{\"error\":\"unknown\"}");
            }
            return repo.save(j);
        }
    }

    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Job not found: " + id);
        }
        repo.deleteById(id);
    }

    private String toJson(Object obj) throws JsonProcessingException {
        return om.writeValueAsString(obj);
    }
}
