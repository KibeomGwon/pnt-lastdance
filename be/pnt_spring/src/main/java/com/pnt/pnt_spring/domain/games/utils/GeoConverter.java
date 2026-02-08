package com.pnt.pnt_spring.domain.games.utils;

import com.pnt.pnt_spring.global.api.code.ErrorCode;
import com.pnt.pnt_spring.global.exception.BusinessException;
import lombok.experimental.UtilityClass;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public final class GeoConverter {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * GeoConverter가 받을 수 있는 "위경도" 타입용 인터페이스
     * - Request DTO의 LatLng 내부 클래스가 이 인터페이스를 implements 하게 만들면
     *   GameRoom / GameMap 어느 쪽이든 공통 변환 가능
     */
    public interface LatLngLike {
        Double getLat();
        Double getLng();
    }

    /** (lat,lng) 리스트 -> Polygon(SRID 4326) */
    public static Polygon toPolygon(List<? extends LatLngLike> points) {
        if (points == null || points.size() < 3) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "polygon은 최소 3개 좌표가 필요합니다.");
        }

        Coordinate[] coords = new Coordinate[points.size() + 1];
        for (int i = 0; i < points.size(); i++) {
            LatLngLike p = points.get(i);
            if (p.getLat() == null || p.getLng() == null) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "polygon 좌표에 null이 포함되어 있습니다.");
            }
            // JTS는 (x=lng, y=lat)
            coords[i] = new Coordinate(p.getLng(), p.getLat());
        }

        // Ring 닫기 (첫 점 = 마지막 점)
        coords[coords.length - 1] = new Coordinate(coords[0]);

        LinearRing shell = GEOMETRY_FACTORY.createLinearRing(coords);
        return GEOMETRY_FACTORY.createPolygon(shell, null);
    }

    /** Polygon -> (lat,lng) 리스트 (마지막 닫는 점은 제거해서 반환) */
    public static List<SimpleLatLng> toLatLngList(Polygon polygon) {
        if (polygon == null) return List.of();

        Coordinate[] coords = polygon.getExteriorRing().getCoordinates();
        if (coords == null || coords.length == 0) return List.of();

        // 마지막 좌표가 첫 좌표와 동일하게 닫혀있을 수 있으니 제거
        if (coords.length >= 2 && coords[0].equals2D(coords[coords.length - 1])) {
            coords = Arrays.copyOf(coords, coords.length - 1);
        }

        List<SimpleLatLng> result = new ArrayList<>(coords.length);
        for (Coordinate c : coords) {
            result.add(new SimpleLatLng(c.getY(), c.getX())); // y=lat, x=lng
        }
        return result;
    }

    /** 응답 DTO 만들 때 쓰기 좋은 간단 LatLng */
    public record SimpleLatLng(Double lat, Double lng) implements LatLngLike {
        @Override public Double getLat() { return lat; }
        @Override public Double getLng() { return lng; }
    }
}
