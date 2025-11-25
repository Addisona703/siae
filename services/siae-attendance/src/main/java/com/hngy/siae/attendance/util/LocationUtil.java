package com.hngy.siae.attendance.util;

import com.hngy.siae.attendance.entity.AttendanceRule;

/**
 * 位置工具类
 * 用于计算地理位置距离和验证位置是否在允许范围内
 *
 * @author SIAE Team
 */
public class LocationUtil {

    private static final double EARTH_RADIUS = 6371000; // 地球半径（米）

    /**
     * 计算两个经纬度坐标之间的距离（米）
     * 使用 Haversine 公式
     *
     * @param lat1 第一个点的纬度
     * @param lon1 第一个点的经度
     * @param lat2 第二个点的纬度
     * @param lon2 第二个点的经度
     * @return 距离（米）
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * 解析位置字符串为经纬度
     * 格式: "latitude,longitude" 例如: "39.9042,116.4074"
     *
     * @param location 位置字符串
     * @return double数组 [纬度, 经度]
     */
    public static double[] parseLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return null;
        }

        String[] parts = location.split(",");
        if (parts.length != 2) {
            return null;
        }

        try {
            double lat = Double.parseDouble(parts[0].trim());
            double lon = Double.parseDouble(parts[1].trim());
            return new double[]{lat, lon};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 验证位置是否在允许范围内
     *
     * @param checkInLocation 签到位置
     * @param allowedLocation 允许的位置
     * @param radiusMeters 允许的半径（米）
     * @return true 如果在范围内，否则 false
     */
    public static boolean isWithinRange(String checkInLocation, String allowedLocation, int radiusMeters) {
        double[] checkIn = parseLocation(checkInLocation);
        double[] allowed = parseLocation(allowedLocation);

        if (checkIn == null || allowed == null) {
            return false;
        }

        double distance = calculateDistance(checkIn[0], checkIn[1], allowed[0], allowed[1]);
        return distance <= radiusMeters;
    }

    /**
     * 验证位置是否在允许范围内（使用 AttendanceRule.Location 对象）
     *
     * @param checkInLocation 签到位置字符串
     * @param allowedLocation 允许的位置对象
     * @param radiusMeters 允许的半径（米）
     * @return true 如果在范围内，否则 false
     */
    public static boolean isWithinRange(String checkInLocation, AttendanceRule.Location allowedLocation, int radiusMeters) {
        double[] checkIn = parseLocation(checkInLocation);

        if (checkIn == null || allowedLocation == null || 
            allowedLocation.getLatitude() == null || allowedLocation.getLongitude() == null) {
            return false;
        }

        double distance = calculateDistance(
            checkIn[0], checkIn[1], 
            allowedLocation.getLatitude(), allowedLocation.getLongitude()
        );
        return distance <= radiusMeters;
    }
}
