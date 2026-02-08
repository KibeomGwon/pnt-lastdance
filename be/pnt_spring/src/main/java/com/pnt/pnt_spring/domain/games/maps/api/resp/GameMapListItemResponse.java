package com.pnt.pnt_spring.domain.games.maps.api.resp;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import com.pnt.pnt_spring.domain.games.maps.entity.GameMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameMapListItemResponse {

    private Long id;
    private String name;
    private String description;
    private OffsetDateTime updatedAt;

    public static GameMapListItemResponse from(GameMap map) {
        return GameMapListItemResponse.builder()
                .id(map.getId())
                .name(map.getName())
                .description(map.getDescription())
                .updatedAt(map.getUpdatedAt())
                .build();
    }
}
