import * as turf from "@turf/turf";

export class TurfService {
    checkUserInBoundary = async (userPoint, boundary) => {
        const userPt = turf.point(userPoint);

        // 경계선의 처음과 끝이 같은지 확인 (LinearRing 조건)
        const start = boundary[0];
        const end = boundary[boundary.length - 1];
        if (start[0] !== end[0] || start[1] !== end[1]) {
            boundary.push(start);
        }

        // turf.polygon은 [[[x,y], [x,y], ...]] 형태의 3중 배열을 받습니다 (첫번째 링이 외곽선)
        const boundaryPt = turf.polygon([boundary]);

        const userErrorRange = turf.buffer(userPt, 5, { units: 'meters' });

        // 2. 유저의 오차 범위 원과 경계선(Boundary)이 겹치는지 확인
        // booleanIntersects는 두 도형이 조금이라도 닿아있으면 true를 반환합니다.
        const isOverlapping = turf.booleanIntersects(userErrorRange, boundaryPt);

        return isOverlapping;
    }

    checkUserInPrison = async (userPoint, prisonLocation) => {
        const userPt = turf.point(userPoint);

        const prisonPt = turf.point(prisonLocation);

        const distance = turf.distance(userPt, prisonPt, { units: 'meters' });

        return distance <= 15;
    }

    /**
     * police = {
     *  policeId: number, lat: number, lng: number
     * }
     */
    checkNearPolice = (thiefPoint, polices) => { // async 제거 (필요없다면)
        const thiefPt = turf.point(thiefPoint); // [lng, lat]

        let resData = null; // 결과가 없을 때 null을 주는 것이 더 명확함
        let minDistance = 15; // 최대 감지 거리

        for (const police of polices) {
            // 순서 주의: [lng, lat]
            const policePt = turf.point([police.lng, police.lat]);
            const distance = turf.distance(thiefPt, policePt, { units: 'meters' });

            // 거리 조건 확인
            if (distance <= 15 && distance < minDistance) {
                minDistance = distance;
                resData = {
                    isNearPolice: true,
                    policeId: police.policeId,
                    distance: distance,
                };
            }
        }

        return resData || false; // 찾았으면 객체, 못 찾았으면 false
    }
}
