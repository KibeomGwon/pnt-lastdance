package com.pnt.pnt_spring.domain.games.maps.application;

import com.pnt.pnt_spring.domain.games.maps.api.req.GameMapCreateRequest;
import com.pnt.pnt_spring.domain.games.maps.entity.GameMap;

import java.util.List;

public interface GameMapService {

    /**
     * 내 맵 저장
     */
    Long createMap(Long memberId, GameMapCreateRequest req);

    /**
     * 내 맵 목록 조회
     */
    List<GameMap> getMyMaps(Long memberId);

    /**
     * 내 맵 단건 조회
     */
    GameMap getMyMap(Long memberId, Long mapId);

    /**
     * 내 맵 삭제 (soft delete)
     */
    void deleteMap(Long memberId, Long mapId);
}
