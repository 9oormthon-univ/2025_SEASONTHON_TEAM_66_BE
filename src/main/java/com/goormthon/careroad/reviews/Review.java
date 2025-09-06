package com.goormthon.careroad.reviews;

import com.goormthon.careroad.common.jpa.BaseTimeEntity;
import com.goormthon.careroad.facilities.Facility;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "reviews",
        indexes = {
                @Index(name = "idx_reviews_facility", columnList = "facility_id"),
                @Index(name = "idx_reviews_rating", columnList = "rating")
        })
public class Review extends BaseTimeEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** 간단화를 위해 사용자 참조는 문자열로 보관. 추후 User 엔티티 연동 예정 */
    @Column(name = "user_ref", length = 100, nullable = false)
    private String userRef;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1~5

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "photo_url", columnDefinition = "text")
    private String photoUrl;

    public Review() { this.id = UUID.randomUUID(); }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUserRef() { return userRef; }
    public void setUserRef(String userRef) { this.userRef = userRef; }
    public Facility getFacility() { return facility; }
    public void setFacility(Facility facility) { this.facility = facility; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
