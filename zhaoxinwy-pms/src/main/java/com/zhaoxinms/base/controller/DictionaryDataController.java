package com.zhaoxinms.base.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhaoxinms.base.ActionResult;
import com.zhaoxinms.base.entity.DictionaryDataEntity;
import com.zhaoxinms.base.entity.DictionaryTypeEntity;
import com.zhaoxinms.base.exception.DataException;
import com.zhaoxinms.base.model.dictionarydata.DictionaryDataAllModel;
import com.zhaoxinms.base.model.dictionarydata.DictionaryDataAllVO;
import com.zhaoxinms.base.model.dictionarydata.DictionaryDataCrForm;
import com.zhaoxinms.base.model.dictionarydata.DictionaryDataInfoVO;
import com.zhaoxinms.base.model.dictionarydata.DictionaryDataListVO;
import com.zhaoxinms.base.model.dictionarydata.DictionaryDataModel;
import com.zhaoxinms.base.model.dictionarydata.DictionaryDataSelectVO;
import com.zhaoxinms.base.model.dictionarydata.DictionaryDataUpForm;
import com.zhaoxinms.base.model.dictionarydata.PageDictionaryData;
import com.zhaoxinms.base.model.dictionarytype.DictionaryTypeSelectModel;
import com.zhaoxinms.base.model.dictionarytype.DictionaryTypeSelectVO;
import com.zhaoxinms.base.service.DictionaryDataService;
import com.zhaoxinms.base.service.DictionaryTypeService;
import com.zhaoxinms.base.util.JsonUtil;
import com.zhaoxinms.base.util.StringUtil;
import com.zhaoxinms.base.util.treeutil.ListToTreeUtil;
import com.zhaoxinms.base.util.treeutil.SumTree;
import com.zhaoxinms.base.util.treeutil.TreeDotUtils;
import com.zhaoxinms.base.vo.ListVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "????????????", value = "DictionaryData")
@RestController
@RequestMapping("/Base/DictionaryData")
public class DictionaryDataController {

    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private DictionaryTypeService dictionaryTypeService;

    /**
     * ????????????????????????
     *
     * @return
     */
    @ApiOperation("????????????????????????")
    @GetMapping("/{dictionaryTypeId}")
    public ActionResult bindDictionary(@PathVariable("dictionaryTypeId") String dictionaryTypeId, PageDictionaryData pageDictionaryData) {
        List<DictionaryDataEntity> data = dictionaryDataService.getList(dictionaryTypeId);
        List<DictionaryDataEntity> dataAll = data;
        if(StringUtil.isNotEmpty(pageDictionaryData.getKeyword())){
            data = data.stream().filter(t->t.getFullName().contains(pageDictionaryData.getKeyword()) || t.getEnCode().contains(pageDictionaryData.getKeyword())).collect(Collectors.toList());
        }
        if (pageDictionaryData.getIsTree() != null && "1".equals(pageDictionaryData.getIsTree())) {
            List<DictionaryDataEntity> treeData = JsonUtil.getJsonToList(ListToTreeUtil.treeWhere(data, dataAll), DictionaryDataEntity.class);
            List<DictionaryDataModel> voListVO = JsonUtil.getJsonToList(treeData, DictionaryDataModel.class);
            List<SumTree<DictionaryDataModel>> sumTrees = TreeDotUtils.convertListToTreeDot(voListVO);
            List<DictionaryDataListVO> list = JsonUtil.getJsonToList(sumTrees, DictionaryDataListVO.class);
            ListVO<DictionaryDataListVO> treeVo = new ListVO<>();
            treeVo.setList(list);
            return ActionResult.success(treeVo);
        }
        List<DictionaryDataModel> voListVO = JsonUtil.getJsonToList(data, DictionaryDataModel.class);
        ListVO<DictionaryDataModel> treeVo = new ListVO<>();
        treeVo.setList(voListVO);
        return ActionResult.success(treeVo);
    }


    /**
     * ????????????????????????
     *
     * @return
     */
    @ApiOperation("????????????????????????(??????+??????)")
    @GetMapping("/All")
    public ActionResult allBindDictionary() {
        List<DictionaryTypeEntity> dictionaryTypeList = dictionaryTypeService.getList();
        List<DictionaryDataEntity> dictionaryDataList = dictionaryDataService.getList().stream().filter(t -> "1".equals(String.valueOf(t.getEnabledMark()))).collect(Collectors.toList());
        List<Map<String, Object>> list = new ArrayList<>();
        for (DictionaryTypeEntity dictionaryTypeEntity : dictionaryTypeList) {
            List<DictionaryDataEntity> childNodeList = dictionaryDataList.stream().filter(t -> dictionaryTypeEntity.getId().equals(t.getDictionaryTypeId())).collect(Collectors.toList());
            if (dictionaryTypeEntity.getIsTree().compareTo(1) == 0) {
                List<Map<String, Object>> selectList = new ArrayList<>();
                for (DictionaryDataEntity item : childNodeList) {
                    Map<String, Object> ht = new HashMap<>();
                    ht.put("fullName", item.getFullName());
                    ht.put("id", item.getId());
                    ht.put("parentId", item.getParentId());
                    selectList.add(ht);
                }
                //==============?????????
                List<SumTree<DictionaryDataAllModel>> list1 = TreeDotUtils.convertListToTreeDot(JsonUtil.getJsonToList(selectList, DictionaryDataAllModel.class));
                List<DictionaryDataAllVO> list2 = JsonUtil.getJsonToList(list1, DictionaryDataAllVO.class);
                //==============
                Map<String, Object> ht_item = new HashMap<>();
                ht_item.put("id", dictionaryTypeEntity.getId());
                ht_item.put("enCode", dictionaryTypeEntity.getEnCode());
                ht_item.put("dictionaryList", list2);
                ht_item.put("isTree", 1);
                list.add(ht_item);
            } else {
                List<Map<String, Object>> selectList = new ArrayList<>();
                for (DictionaryDataEntity item : childNodeList) {
                    Map<String, Object> ht = new HashMap<>();
                    ht.put("enCode", item.getEnCode());
                    ht.put("id", item.getId());
                    ht.put("fullName", item.getFullName());
                    selectList.add(ht);
                }
                Map<String, Object> ht_item = new HashMap<>();
                ht_item.put("id", dictionaryTypeEntity.getId());
                ht_item.put("enCode", dictionaryTypeEntity.getEnCode());
                ht_item.put("dictionaryList", selectList);
                ht_item.put("isTree", 0);
                list.add(ht_item);
            }
        }
        ListVO<Map<String, Object>> vo = new ListVO<>();
        vo.setList(list);
        return ActionResult.success(vo);
    }


    /**
     * ?????????????????????????????????
     *
     * @param dictionaryTypeId ????????????
     * @return
     */
    @ApiOperation("???????????????????????????????????????")
    @GetMapping("{dictionaryTypeId}/Selector")
    public ActionResult treeView(@PathVariable("dictionaryTypeId") String dictionaryTypeId, String isTree) {

        DictionaryTypeEntity typeEntity = dictionaryTypeService.getInfo(dictionaryTypeId);
        List<DictionaryDataModel> treeList = new ArrayList<>();
        DictionaryDataModel treeViewModel = new DictionaryDataModel();
        treeViewModel.setId("0");
        treeViewModel.setFullName(typeEntity.getFullName());
        treeViewModel.setParentId("-1");
        treeViewModel.setIcon("fa fa-tags");
        treeList.add(treeViewModel);
        if (isTree != null && "1".equals(isTree)) {
            List<DictionaryDataEntity> data = dictionaryDataService.getList(dictionaryTypeId);
            for (DictionaryDataEntity entity : data) {
                DictionaryDataModel treeModel = new DictionaryDataModel();
                treeModel.setId(entity.getId());
                treeModel.setFullName(entity.getFullName());
                treeModel.setParentId("-1".equals(entity.getParentId()) ? entity.getDictionaryTypeId() : entity.getParentId());
                treeList.add(treeModel);
            }
        }
        List<SumTree<DictionaryDataModel>> sumTrees = TreeDotUtils.convertListToTreeDot(treeList);
        List<DictionaryDataSelectVO> list = JsonUtil.getJsonToList(sumTrees, DictionaryDataSelectVO.class);
        ListVO<DictionaryDataSelectVO> treeVo = new ListVO<>();
        treeVo.setList(list);
        return ActionResult.success(treeVo);
    }

    /**
     * ??????????????????
     *
     * @return
     */
    @ApiOperation("???????????????????????????????????????")
    @GetMapping("/{dictionaryTypeId}/Data/Selector")
    public ActionResult selectorOneTreeView(@PathVariable("dictionaryTypeId") String dictionaryTypeId) {
        List<DictionaryDataEntity> data = dictionaryDataService.getList().stream().filter(t -> dictionaryTypeId.equals(t.getDictionaryTypeId())).collect(Collectors.toList());
        List<DictionaryTypeSelectModel> voListVO = JsonUtil.getJsonToList(data, DictionaryTypeSelectModel.class);
        List<SumTree<DictionaryTypeSelectModel>> sumTrees = TreeDotUtils.convertListToTreeDot(voListVO);
        List<DictionaryTypeSelectVO> list = JsonUtil.getJsonToList(sumTrees, DictionaryTypeSelectVO.class);
        ListVO<DictionaryTypeSelectVO> vo = new ListVO<>();
        vo.setList(list);
        return ActionResult.success(vo);
    }


    /**
     * ????????????????????????
     *
     * @param id ?????????
     * @return
     */
    @ApiOperation("????????????????????????")
    @GetMapping("/{id}/Info")
    public ActionResult info(@PathVariable("id") String id) throws DataException {
        DictionaryDataEntity entity = dictionaryDataService.getInfo(id);
        DictionaryDataInfoVO vo = JsonUtil.getJsonToBeanEx(entity, DictionaryDataInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * ????????????????????????
     *
     * @param dictionaryTypeId ????????????
     * @param fullName         ??????
     * @param id               ?????????
     * @return
     */
    @ApiOperation("????????????????????????????????????")
    @GetMapping("/IsExistByFullName")
    public ActionResult isExistByFullName(String dictionaryTypeId, String fullName, String id) {
        boolean data = dictionaryDataService.isExistByFullName(dictionaryTypeId, fullName, id);
        return ActionResult.success(data);
    }

    /**
     * ????????????????????????
     *
     * @param dictionaryTypeId ????????????
     * @param enCode           ??????
     * @param id               ?????????
     * @return
     */
    @ApiOperation("????????????????????????????????????")
    @GetMapping("/IsExistByEnCode")
    public ActionResult isExistByEnCode(String dictionaryTypeId, String enCode, String id) {
        boolean data = dictionaryDataService.isExistByEnCode(dictionaryTypeId, enCode, id);
        return ActionResult.success(data);
    }


    /**
     * ??????????????????
     *
     * @param dictionaryDataCrForm ????????????
     * @return
     */
    @ApiOperation("??????????????????")
    @PostMapping
    public ActionResult create(@RequestBody @Valid DictionaryDataCrForm dictionaryDataCrForm) {
        DictionaryDataEntity entity = JsonUtil.getJsonToBean(dictionaryDataCrForm, DictionaryDataEntity.class);
        if (dictionaryDataService.isExistByFullName(entity.getDictionaryTypeId(), entity.getFullName(), entity.getId())) {
            return ActionResult.fail("????????????????????????");
        }
        if (dictionaryDataService.isExistByEnCode(entity.getDictionaryTypeId(), entity.getEnCode(), entity.getId())) {
            return ActionResult.fail("????????????????????????");
        }
        dictionaryDataService.create(entity);
        return ActionResult.success("????????????");
    }

    /**
     * ??????????????????
     *
     * @param dictionaryDataUpForm ????????????
     * @param id                   ?????????
     * @return
     */
    @ApiOperation("??????????????????")
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid DictionaryDataUpForm dictionaryDataUpForm) {
        DictionaryDataEntity entity = JsonUtil.getJsonToBean(dictionaryDataUpForm, DictionaryDataEntity.class);
        if (dictionaryDataService.isExistByFullName(entity.getDictionaryTypeId(), entity.getFullName(), id)) {
            return ActionResult.fail("????????????????????????");
        }
        if (dictionaryDataService.isExistByEnCode(entity.getDictionaryTypeId(), entity.getEnCode(), id)) {
            return ActionResult.fail("????????????????????????");
        }
        entity.setEnCode(null);
        boolean flag = dictionaryDataService.update(id, entity);
        if (flag == false) {
            return ActionResult.success("??????????????????????????????");
        }
        return ActionResult.success("????????????");

    }

    /**
     * ??????????????????
     *
     * @param id ?????????
     * @return
     */
    @ApiOperation("??????????????????")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        DictionaryDataEntity entity = dictionaryDataService.getInfo(id);
        if (entity != null) {
            dictionaryDataService.delete(entity);
            return ActionResult.success("????????????");
        }
        return ActionResult.fail("??????????????????????????????");
    }

    /**
     * ??????????????????
     *
     * @param id ?????????
     * @return
     */
    @ApiOperation("??????????????????")
    @PutMapping("/{id}/Actions/State")
    public ActionResult update(@PathVariable("id") String id) {
        DictionaryDataEntity entity = dictionaryDataService.getInfo(id);
        if (entity != null) {
            if (entity.getEnabledMark() == 1) {
                entity.setEnabledMark(0);
            } else {
                entity.setEnabledMark(1);
            }
            boolean flag = dictionaryDataService.update(entity.getId(), entity);
            if (flag == false) {
                return ActionResult.success("??????????????????????????????");
            }
        }
        return ActionResult.success("????????????");
    }

    /**
     * ??????????????????????????????
     */
    @GetMapping("/getList/{dictionary}")
    public ActionResult getList(@PathVariable("dictionary") String dictionary){
        List<DictionaryDataEntity> list = dictionaryDataService.getList(dictionary);
        return ActionResult.success(list);
    }

    /**
     * ????????????????????????
     */
    @GetMapping("/getListAll")
    public ActionResult getListAll(){
        List<DictionaryDataEntity> list = dictionaryDataService.getList();
        return ActionResult.success(list);
    }

    /**
     * ????????????????????????
     *
     * @param id ?????????
     * @return
     */
    @ApiOperation("????????????????????????")
    @GetMapping("/{id}/info")
    public ActionResult getInfo(@PathVariable("id") String id) throws DataException {
        DictionaryDataEntity entity = dictionaryDataService.getInfo(id);
        return ActionResult.success(entity);
    }

}
