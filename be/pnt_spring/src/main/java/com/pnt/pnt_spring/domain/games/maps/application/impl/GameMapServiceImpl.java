package com.pnt.pnt_spring.domain.games.maps.application.impl;


import com.pnt.pnt_spring.domain.games.maps.api.req.GameMapCreateRequest;
import com.pnt.pnt_spring.domain.games.maps.application.GameMapService;
import com.pnt.pnt_spring.domain.games.maps.entity.GameMap;
import com.pnt.pnt_spring.domain.games.maps.repository.GameMapRepository;
import com.pnt.pnt_spring.domain.games.utils.GeoConverter;
import com.pnt.pnt_spring.global.api.code.ErrorCode;
import com.pnt.pnt_spring.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.locationtech.jts.geom.Polygon;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GameMapServiceImpl implements GameMapService {

    private final GameMapRepository gameMapRepository;

    @Override
    public Long createMap(Long memberId, GameMapCreateRequest req) {

        Polygon polygon = GeoConverter.toPolygon(req.getPolygon());

        GameMap map = GameMap.create(
                memberId,
                req.getName(),
                req.getDescription(),
                polygon,
                req.getPrison().getLat(),
                req.getPrison().getLng()
        );

        gameMapRepository.save(map);
        return map.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameMap> getMyMaps(Long memberId) {
        return gameMapRepository.findAllByOwnerIdAndIsDeletedFalseOrderByUpdatedAtDesc(memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public GameMap getMyMap(Long memberId, Long mapId) {
        return gameMapRepository.findByIdAndOwnerIdAndIsDeletedFalse(mapId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MAP_NOT_FOUND, "맵을 찾을 수 없습니다."));
    }

    @Override
    public void deleteMap(Long memberId, Long mapId) {
        GameMap map = gameMapRepository.findByIdAndOwnerIdAndIsDeletedFalse(mapId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MAP_NOT_FOUND, "맵을 찾을 수 없습니다."));

        map.delete();
    }
}
