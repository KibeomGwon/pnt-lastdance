package com.pnt.pnt_spring.domain.games.maps.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.pnt.pnt_spring.domain.games.maps.api.req.GameMapCreateRequest;
import com.pnt.pnt_spring.domain.games.maps.api.resp.GameMapDetailResponse;
import com.pnt.pnt_spring.domain.games.maps.api.resp.GameMapListItemResponse;
import com.pnt.pnt_spring.domain.games.maps.application.GameMapService;
import com.pnt.pnt_spring.domain.games.maps.entity.GameMap;
import com.pnt.pnt_spring.global.api.response.CommonResponse;
import com.pnt.pnt_spring.global.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/maps")
@Tag(name = "Game Map", description = "유저 저장 맵 관련 API")
public class GameMapController {

    private final GameMapService gameMapService;

    @Operation(summary = "내 맵 저장")
    @PostMapping
    public CommonResponse<Long> createMap(@Valid @RequestBody GameMapCreateRequest req) {
        Long memberId = SecurityUtils.currentMemberId();
        Long mapId = gameMapService.createMap(memberId, req);

        return new CommonResponse<>(
                mapId,
                "맵 저장 성공",
                HttpStatus.CREATED
        );
    }

    @Operation(summary = "내 맵 목록 조회")
    @GetMapping
    public CommonResponse<List<GameMapListItemResponse>> getMyMaps() {
        Long memberId = SecurityUtils.currentMemberId();
        List<GameMap> maps = gameMapService.getMyMaps(memberId);

        List<GameMapListItemResponse> data = maps.stream()
                .map(GameMapListItemResponse::from)
                .toList();

        return new CommonResponse<>(
                data,
                "내 맵 목록 조회 성공",
                HttpStatus.OK
        );
    }

    @Operation(summary = "내 맵 단건 조회")
    @GetMapping("/{mapId}")
    public CommonResponse<GameMapDetailResponse> getMyMap(@PathVariable Long mapId) {
        Long memberId = SecurityUtils.currentMemberId();
        GameMap map = gameMapService.getMyMap(memberId, mapId);

        GameMapDetailResponse data = GameMapDetailResponse.from(map);

        return new CommonResponse<>(
                data,
                "내 맵 조회 성공",
                HttpStatus.OK
        );
    }

    @Operation(summary = "내 맵 삭제(soft delete)")
    @DeleteMapping("/{mapId}")
    public CommonResponse<Void> deleteMap(@PathVariable Long mapId) {
        Long memberId = SecurityUtils.currentMemberId();
        gameMapService.deleteMap(memberId, mapId);

        return new CommonResponse<>(
                null,
                "내 맵 삭제 성공",
                HttpStatus.OK
        );
    }
}
