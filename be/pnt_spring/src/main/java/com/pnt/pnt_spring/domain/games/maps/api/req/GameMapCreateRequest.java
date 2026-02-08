package com.pnt.pnt_spring.domain.games.maps.api.req;


import com.pnt.pnt_spring.domain.games.utils.GeoConverter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GameMapCreateRequest {

    @NotBlank(message = "맵 이름은 필수입니다.")
    private String name;

    private String description;

    @Valid
    @NotNull(message = "감옥 좌표(prison)는 필수입니다.")
    private Prison prison;

    @Valid
    @NotEmpty(message = "polygon은 최소 3개 좌표가 필요합니다.")
    private List<LatLng> polygon;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class Prison {
        private Double lat;

        private Double lng;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class LatLng implements GeoConverter.LatLngLike {
        @NotNull(message = "polygon.lat은 필수입니다.")
        private Double lat;

        @NotNull(message = "polygon.lng은 필수입니다.")
        private Double lng;

        @Override
        public Double getLat() {
            return lat;
        }

        @Override
        public Double getLng() {
            return lng;
        }
    }
}
