package com.pnt.pnt_spring.domain.games.maps.api.resp;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import com.pnt.pnt_spring.domain.games.maps.entity.GameMap;
import com.pnt.pnt_spring.domain.games.utils.GeoConverter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameMapDetailResponse {

    private Long id;
    private String name;
    private String description;

    private Prison prison;
    private List<LatLng> polygon;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Prison {
        private Double lat;
        private Double lng;

        public static Prison of(Double lat, Double lng) {
            return Prison.builder().lat(lat).lng(lng).build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LatLng {
        private Double lat;
        private Double lng;

        public static LatLng of(Double lat, Double lng) {
            return LatLng.builder().lat(lat).lng(lng).build();
        }
    }

    public static GameMapDetailResponse from(GameMap map) {
        List<GeoConverter.SimpleLatLng> list = GeoConverter.toLatLngList(map.getPolygon());

        return GameMapDetailResponse.builder()
                .id(map.getId())
                .name(map.getName())
                .description(map.getDescription())
                .prison(Prison.of(map.getPrisonLat(), map.getPrisonLng()))
                .polygon(list.stream().map(p -> LatLng.of(p.lat(), p.lng())).toList())
                .createdAt(map.getCreatedAt())
                .updatedAt(map.getUpdatedAt())
                .build();
    }
}
