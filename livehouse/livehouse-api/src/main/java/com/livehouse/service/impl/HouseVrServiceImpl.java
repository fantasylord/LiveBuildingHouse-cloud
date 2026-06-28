package com.livehouse.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livehouse.common.exception.BusinessException;
import com.livehouse.entity.HouseBuilding;
import com.livehouse.entity.HouseUnit;
import com.livehouse.entity.HouseVr;
import com.livehouse.mapper.HouseBuildingMapper;
import com.livehouse.mapper.HouseUnitMapper;
import com.livehouse.mapper.HouseVrMapper;
import com.livehouse.service.HouseVrService;
import com.livehouse.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HouseVrServiceImpl extends ServiceImpl<HouseVrMapper, HouseVr> implements HouseVrService {

    @Resource
    private HouseBuildingMapper houseBuildingMapper;

    @Resource
    private HouseUnitMapper houseUnitMapper;

    @Override
    public Page<HouseVr> listPage(int pageNum, int pageSize, Long buildingId, String buildingName,
                                  Long unitId, String vrName, Integer status) {
        Page<HouseVr> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HouseVr> wrapper = new LambdaQueryWrapper<>();

        if (buildingId != null) {
            wrapper.eq(HouseVr::getBuildingId, buildingId);
        }
        if (unitId != null) {
            wrapper.eq(HouseVr::getUnitId, unitId);
        }
        if (StringUtils.hasText(vrName)) {
            wrapper.like(HouseVr::getVrName, vrName);
        }
        if (status != null) {
            wrapper.eq(HouseVr::getStatus, status);
        }

        wrapper.eq(HouseVr::getDeleted, 0)
                .orderByDesc(HouseVr::getCreateTime);

        Page<HouseVr> result = this.page(page, wrapper);
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            fillBuildingAndUnitNames(result.getRecords(), buildingName);
        }
        return result;
    }

    private void fillBuildingAndUnitNames(List<HouseVr> vrList, String buildingName) {
        if (vrList == null || vrList.isEmpty()) {
            return;
        }

        List<Long> buildingIds = vrList.stream()
                .map(HouseVr::getBuildingId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        List<Long> unitIds = vrList.stream()
                .map(HouseVr::getUnitId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> buildingNameMap = new HashMap<>();
        if (!buildingIds.isEmpty()) {
            List<HouseBuilding> buildings = houseBuildingMapper.selectBatchIds(buildingIds);
            for (HouseBuilding building : buildings) {
                buildingNameMap.put(building.getId(), building.getBuildingName());
            }
        }

        Map<Long, String> unitNameMap = new HashMap<>();
        if (!unitIds.isEmpty()) {
            List<HouseUnit> units = houseUnitMapper.selectBatchIds(unitIds);
            for (HouseUnit unit : units) {
                unitNameMap.put(unit.getId(), unit.getUnitName());
            }
        }

        for (HouseVr vr : vrList) {
            if (vr.getBuildingId() != null) {
                vr.setBuildingName(buildingNameMap.get(vr.getBuildingId()));
            }
            if (vr.getUnitId() != null) {
                vr.setUnitName(unitNameMap.get(vr.getUnitId()));
            }
        }
    }

    @Override
    public HouseVr getDetail(Long id) {
        HouseVr vr = this.baseMapper.selectById(id);
        if (vr == null) {
            return null;
        }
        if (vr.getBuildingId() != null) {
            HouseBuilding building = houseBuildingMapper.selectById(vr.getBuildingId());
            if (building != null) {
                vr.setBuildingName(building.getBuildingName());
            }
        }
        if (vr.getUnitId() != null) {
            HouseUnit unit = houseUnitMapper.selectById(vr.getUnitId());
            if (unit != null) {
                vr.setUnitName(unit.getUnitName());
            }
        }
        return vr;
    }

    @Override
    public boolean addVr(HouseVr vr) {
        if (vr == null) {
            return false;
        }
        validateAndNormalizeVr(vr);
        return this.save(vr);
    }

    @Override
    public boolean updateVr(HouseVr vr) {
        if (vr == null || vr.getId() == null) {
            return false;
        }
        if (this.baseMapper.selectById(vr.getId()) == null) {
            throw new BusinessException(404, "VR resource does not exist");
        }
        validateAndNormalizeVr(vr);
        return this.updateById(vr);
    }

    @Override
    public boolean deleteVr(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean changeStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return false;
        }
        if (status != 0 && status != 1) {
            throw new BusinessException(400, "VR status must be 0 or 1");
        }
        HouseVr vr = new HouseVr();
        vr.setId(id);
        vr.setStatus(status);
        return this.updateById(vr);
    }

    private void validateAndNormalizeVr(HouseVr vr) {
        if (vr.getBuildingId() == null) {
            throw new BusinessException(400, "Please select a building");
        }
        if (!StringUtils.hasText(vr.getVrName())) {
            throw new BusinessException(400, "Please enter a VR name");
        }
        if (!StringUtils.hasText(vr.getPanoramaUrl()) && !StringUtils.hasText(vr.getScenes())) {
            throw new BusinessException(400, "Please configure at least one panorama image");
        }
        if (vr.getStatus() != null && vr.getStatus() != 0 && vr.getStatus() != 1) {
            throw new BusinessException(400, "VR status must be 0 or 1");
        }
        if (vr.getPrivated() != null && vr.getPrivated() != 0 && vr.getPrivated() != 1) {
            throw new BusinessException(400, "Private status must be 0 or 1");
        }
        if (houseBuildingMapper.selectById(vr.getBuildingId()) == null) {
            throw new BusinessException(400, "Building does not exist");
        }
        if (vr.getUnitId() != null) {
            HouseUnit unit = houseUnitMapper.selectById(vr.getUnitId());
            if (unit == null) {
                throw new BusinessException(400, "House unit does not exist");
            }
            if (unit.getBuildingId() != null && !unit.getBuildingId().equals(vr.getBuildingId())) {
                throw new BusinessException(400, "House unit does not belong to the selected building");
            }
        }
        normalizeScenes(vr);
    }

    private void normalizeScenes(HouseVr vr) {
        try {
            JSONObject root = parseScenesRoot(vr);
            JSONArray scenes = normalizeSceneArray(root, vr);
            root.put("scenes", scenes);
            validateSceneTargets(scenes);
            vr.setScenes(JsonUtils.toJsonString(root));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("VR scene config parse failed", e);
            throw new BusinessException(400, "Invalid VR scene JSON");
        }
    }

    private JSONObject parseScenesRoot(HouseVr vr) {
        if (!StringUtils.hasText(vr.getScenes())) {
            JSONObject root = new JSONObject();
            JSONArray scenes = new JSONArray();
            JSONObject defaultScene = new JSONObject();
            defaultScene.put("id", "default");
            defaultScene.put("code", "default");
            defaultScene.put("name", vr.getVrName());
            defaultScene.put("shortName", "VR");
            defaultScene.put("panoramaUrl", safeText(vr.getPanoramaUrl()));
            defaultScene.put("thumbnailUrl", safeText(vr.getThumbnailUrl()));
            defaultScene.put("modelX", 50);
            defaultScene.put("modelY", 50);
            defaultScene.put("initialLon", 0);
            defaultScene.put("initialLat", 0);
            defaultScene.put("hotspots", new JSONArray());
            scenes.add(defaultScene);
            root.put("scenes", scenes);
            return root;
        }

        Object parsed = JsonUtils.parse(vr.getScenes());
        if (parsed instanceof JSONArray) {
            JSONObject root = new JSONObject();
            root.put("scenes", parsed);
            return root;
        }
        if (parsed instanceof JSONObject) {
            return (JSONObject) parsed;
        }
        throw new BusinessException(400, "Scene config must be a JSON object or array");
    }

    private JSONArray normalizeSceneArray(JSONObject root, HouseVr vr) {
        Object scenesNode = root.get("scenes");
        if (!(scenesNode instanceof JSONArray) || ((JSONArray) scenesNode).isEmpty()) {
            throw new BusinessException(400, "Please configure at least one VR scene");
        }

        Set<String> sceneIds = new HashSet<>();
        JSONArray normalizedScenes = new JSONArray();
        int index = 0;
        for (Object sceneNode : (JSONArray) scenesNode) {
            if (!(sceneNode instanceof JSONObject)) {
                throw new BusinessException(400, "Scene " + (index + 1) + " must be a JSON object");
            }
            JSONObject scene = JsonUtils.toJsonObject(sceneNode);
            String sceneId = firstText(scene, "id", "sceneId", "code", "sceneCode");
            if (!StringUtils.hasText(sceneId)) {
                sceneId = index == 0 ? "default" : "scene-" + (index + 1);
            }
            if (!sceneIds.add(sceneId)) {
                throw new BusinessException(400, "Duplicate scene id: " + sceneId);
            }

            String sceneName = firstText(scene, "name", "title", "sceneName");
            if (!StringUtils.hasText(sceneName)) {
                sceneName = index == 0 ? vr.getVrName() : "Scene " + (index + 1);
            }

            String panoramaUrl = firstText(scene, "panoramaUrl", "imageUrl", "url");
            if (!StringUtils.hasText(panoramaUrl) && index == 0) {
                panoramaUrl = safeText(vr.getPanoramaUrl());
            }
            if (!StringUtils.hasText(panoramaUrl)) {
                throw new BusinessException(400, sceneName + " has no panorama image");
            }

            String sceneCode = firstText(scene, "code", "sceneCode");
            scene.put("id", sceneId);
            scene.put("code", StringUtils.hasText(sceneCode) ? sceneCode : sceneId);
            scene.put("name", sceneName);
            scene.put("shortName", StringUtils.hasText(scene.getString("shortName"))
                    ? scene.getString("shortName") : buildShortName(sceneName, index));
            scene.put("panoramaUrl", panoramaUrl);
            scene.put("thumbnailUrl", firstText(scene, "thumbnailUrl", "thumbUrl", "coverImage"));
            scene.put("modelX", normalizePercent(scene.get("modelX"), scene.get("x"), sceneName + " modelX"));
            scene.put("modelY", normalizePercent(scene.get("modelY"), scene.get("y"), sceneName + " modelY"));
            scene.put("initialLon", normalizeRange(scene.get("initialLon"), scene.get("lon"), -360, 360,
                    sceneName + " initialLon", 0));
            scene.put("initialLat", normalizeRange(scene.get("initialLat"), scene.get("lat"), -80, 80,
                    sceneName + " initialLat", 0));
            scene.put("hotspots", normalizeSceneHotspots(scene, sceneName));
            normalizedScenes.add(scene);
            index++;
        }
        return normalizedScenes;
    }

    private JSONArray normalizeSceneHotspots(JSONObject scene, String sceneName) {
        Object hotspotsNode = scene.get("hotspots");
        JSONArray normalizedHotspots = new JSONArray();
        if (hotspotsNode == null) {
            return normalizedHotspots;
        }
        if (!(hotspotsNode instanceof JSONArray)) {
            throw new BusinessException(400, sceneName + " hotspots must be an array");
        }

        int index = 0;
        for (Object hotspotNode : (JSONArray) hotspotsNode) {
            if (!(hotspotNode instanceof JSONObject)) {
                throw new BusinessException(400, sceneName + " hotspot " + (index + 1) + " must be a JSON object");
            }
            JSONObject hotspot = JsonUtils.toJsonObject(hotspotNode);
            hotspot.put("x", normalizePercent(hotspot.get("x"), null, sceneName + " hotspot x"));
            hotspot.put("y", normalizePercent(hotspot.get("y"), null, sceneName + " hotspot y"));

            String title = firstText(hotspot, "title", "name");
            if (!StringUtils.hasText(title)) {
                title = "Hotspot " + (index + 1);
            }
            hotspot.put("title", title);

            boolean hasTarget = StringUtils.hasText(firstText(hotspot,
                    "targetSceneId", "targetSceneCode", "targetScene", "sceneId", "sceneCode", "targetVrId", "targetId"));
            hotspot.put("type", StringUtils.hasText(hotspot.getString("type"))
                    ? hotspot.getString("type") : (hasTarget ? "scene" : "info"));
            normalizedHotspots.add(hotspot);
            index++;
        }
        return normalizedHotspots;
    }

    private void validateSceneTargets(JSONArray scenes) {
        Set<String> sceneRefs = new HashSet<>();
        for (Object item : scenes) {
            JSONObject scene = (JSONObject) item;
            sceneRefs.add(scene.getString("id"));
            sceneRefs.add(scene.getString("code"));
            sceneRefs.add(scene.getString("name"));
        }

        for (Object item : scenes) {
            JSONObject scene = (JSONObject) item;
            String sceneName = defaultText(scene.getString("name"), "Scene");
            Object hotspots = scene.get("hotspots");
            if (!(hotspots instanceof JSONArray)) {
                continue;
            }
            for (Object hotspotItem : (JSONArray) hotspots) {
                JSONObject hotspot = (JSONObject) hotspotItem;
                String target = firstText(hotspot, "targetSceneId", "targetSceneCode", "targetScene", "sceneId", "sceneCode");
                if (StringUtils.hasText(target) && !sceneRefs.contains(target)) {
                    String title = defaultText(hotspot.getString("title"), "Unnamed hotspot");
                    throw new BusinessException(400, sceneName + " hotspot " + title + " points to missing scene: " + target);
                }
            }
        }
    }

    private int normalizePercent(Object primary, Object fallback, String fieldName) {
        Object value = primary != null ? primary : fallback;
        if (!(value instanceof Number)) {
            return 50;
        }
        int result = ((Number) value).intValue();
        if (result < 0 || result > 100) {
            throw new BusinessException(400, fieldName + " must be between 0 and 100");
        }
        return result;
    }

    private int normalizeRange(Object primary, Object fallback, int min, int max, String fieldName, int defaultValue) {
        Object value = primary != null ? primary : fallback;
        if (!(value instanceof Number)) {
            return defaultValue;
        }
        int result = ((Number) value).intValue();
        if (result < min || result > max) {
            throw new BusinessException(400, fieldName + " must be between " + min + " and " + max);
        }
        return result;
    }

    private String firstText(JSONObject node, String... fields) {
        for (String field : fields) {
            String value = node.getString(field);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private String buildShortName(String sceneName, int index) {
        if (!StringUtils.hasText(sceneName)) {
            return String.valueOf(index + 1);
        }
        return sceneName.length() <= 2 ? sceneName : sceneName.substring(0, 2);
    }
}
