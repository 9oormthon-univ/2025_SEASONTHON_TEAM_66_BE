package com.goormthon.careroad.facilities;

import com.goormthon.careroad.facilities.dto.FacilityCreateRequest;
import com.goormthon.careroad.facilities.dto.FacilityUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class FacilityService {

    private final FacilityRepository repo;

    public FacilityService(FacilityRepository repo) { this.repo = repo; }

    public Page<Facility> search(String name, String grade, Pageable pageable) {
        Specification<Facility> spec = org.springframework.data.jpa.domain.Specification.allOf(
                FacilitySpecs.nameContains(name),
                FacilitySpecs.gradeEquals(grade)
        );
        return repo.findAll(spec, pageable);
    }

    public Optional<Facility> findById(UUID id) { return repo.findById(id); }

    public Facility create(FacilityCreateRequest req) {
        Facility f = new Facility();
        f.setName(req.name);
        f.setAddress(req.address);
        f.setPhone(req.phone);
        f.setGrade(req.grade);
        f.setCapacity(req.capacity);
        return repo.save(f);
    }

    public Facility update(UUID id, FacilityUpdateRequest req) {
        Facility f = repo.findById(id).orElseThrow(() ->
                new com.goormthon.careroad.common.BusinessException(
                        com.goormthon.careroad.common.ErrorCode.NOT_FOUND, "Facility not found: " + id));

        if (req.name != null) f.setName(req.name);
        if (req.address != null) f.setAddress(req.address);
        if (req.phone != null) f.setPhone(req.phone);
        if (req.grade != null) f.setGrade(req.grade);
        if (req.capacity != null) f.setCapacity(req.capacity);
        return repo.save(f);
    }

    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw new com.goormthon.careroad.common.BusinessException(
                    com.goormthon.careroad.common.ErrorCode.NOT_FOUND, "Facility not found: " + id);
        }
        repo.deleteById(id);
    }
}
