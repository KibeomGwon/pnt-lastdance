package com.pnt.pnt_spring.domain.games.maps.entity;

import com.pnt.pnt_spring.domain.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Polygon;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "maps")
public class GameMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String description;

    // PostGIS Polygon
    @Column(columnDefinition = "geometry")
    private Polygon polygon;

    @Column(name = "prison_lat", nullable = false)
    private Double prisonLat;

    @Column(name = "prison_lng", nullable = false)
    private Double prisonLng;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    /* =========================
       생성 메서드
       ========================= */
    public static GameMap create(
            Long ownerId,
            String name,
            String description,
            Polygon polygon,
            Double prisonLat,
            Double prisonLng
    ) {
        GameMap m = new GameMap();
        m.ownerId = ownerId;
        m.name = name;
        m.description = description;
        m.polygon = polygon;
        m.prisonLat = prisonLat;
        m.prisonLng = prisonLng;
        m.isDeleted = false;
        return m;
    }

    /* =========================
       변경 메서드 (필요 시)
       ========================= */
    public void update(String name, String description) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
    }

    /* =========================
       삭제(soft delete)
       ========================= */
    public void delete() {
        this.isDeleted = true;
    }

    /* =========================
       권한 체크용
       ========================= */
    public boolean isOwner(Long memberId) {
        return this.ownerId != null && this.ownerId.equals(memberId);
    }
}
