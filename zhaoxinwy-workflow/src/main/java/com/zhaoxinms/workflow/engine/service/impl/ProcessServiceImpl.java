package com.zhaoxinms.workflow.engine.service.impl;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.zhaoxinms.common.constant.HttpStatus;
import com.zhaoxinms.common.core.domain.entity.SysUser;
import com.zhaoxinms.common.core.page.TableDataInfo;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.system.mapper.SysUserMapper;
import com.zhaoxinms.util.DateUtils;
import com.zhaoxinms.workflow.engine.entity.HistoricActivity;
import com.zhaoxinms.workflow.engine.entity.MyApplyVo;
import com.zhaoxinms.workflow.engine.entity.TaskVo;
import com.zhaoxinms.workflow.engine.event.WorkflowEvent;
import com.zhaoxinms.workflow.engine.mapper.TaskMapper;
import com.zhaoxinms.workflow.engine.service.IProcessService;

import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class ProcessServiceImpl implements IProcessService {

    protected final Logger logger = LoggerFactory.getLogger(ProcessServiceImpl.class);

    private IdentityService identityService;
    private TaskService taskService;
    private HistoryService historyService;
    private RuntimeService runtimeService;
    private SysUserMapper userMapper;
    private TaskMapper taskMapper;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private static final String INSTANCE_TITLE = "INSTANCE_TITLE";
    private static final String BUSINESS_NO = "BUSINESS_NO";

    /**
     * ????????????
     */
    @Override
    public <T> void submitApply(T entity, String key, String title, String businessNo) throws Exception {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put(INSTANCE_TITLE, title);
        variables.put(BUSINESS_NO, businessNo);
        this.submitApply(entity, title, key, variables);
    }

    @Override
    public <T> void submitApply(T entity, String key, String title, String businessNo, Map<String, Object> variables) throws Exception {
        variables.put(INSTANCE_TITLE, title);
        variables.put(BUSINESS_NO, businessNo);
        this.submitApply(entity, title, key, variables);
    }

    private <T> void submitApply(T entity, String name, String key, Map<String, Object> variables) throws Exception {

        Class clazz = entity.getClass();

        Method getId = clazz.getDeclaredMethod("getId");
        Long id = Long.valueOf((String)getId.invoke(entity));

        Method setInstanceId = clazz.getDeclaredMethod("setInstanceId", String.class);

        String username = SecurityUtils.getUsername();

        // ?????????????????????????????????ID???????????????????????????ID?????????activiti:initiator???
        identityService.setAuthenticatedUserId(username);
        // ??????????????????????????? key
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(key, id + "", variables);
        //?????????????????????name
        runtimeService.setProcessInstanceName(instance.getId(),name);
        // ???????????????????????????id??????
        setInstanceId.invoke(entity, instance.getId());
    }

    /** ?????????????????? */
    private String humpToLine(String str) {
        Pattern humpPattern = Pattern.compile("[A-Z]");
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /** ?????????????????? */
    public static String lineToHump(String str) {
        str = str.toLowerCase();
        Pattern linePattern = Pattern.compile("_(\\w)");
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * ????????????????????????
     */
    @Override
    public <T> void richProcessField(T entity) throws Exception {
        Class clazz = entity.getClass();
        Method getInstanceId = clazz.getDeclaredMethod("getInstanceId");
        String instanceId = (String)getInstanceId.invoke(entity);

        Method setTaskId = clazz.getSuperclass().getDeclaredMethod("setTaskId", String.class);
        Method setTaskName = clazz.getSuperclass().getDeclaredMethod("setTaskName", String.class);
        Method setTaskDefKey = clazz.getSuperclass().getDeclaredMethod("setTaskDefKey", String.class);
        Method setSuspendState = clazz.getSuperclass().getDeclaredMethod("setSuspendState", String.class);
        Method setSuspendStateName = clazz.getSuperclass().getDeclaredMethod("setSuspendStateName", String.class);

        // ????????????
        if (StringUtils.isNotBlank(instanceId)) {
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(instanceId).list(); // ????????????????????????????????????????????????
            if (!CollectionUtils.isEmpty(taskList)) {
                TaskEntityImpl task = (TaskEntityImpl)taskList.get(0);

                setTaskId.invoke(entity, task.getId());
                setTaskDefKey.invoke(entity, task.getTaskDefinitionKey());
                if (task.getSuspensionState() == 2) {
                    setTaskName.invoke(entity, "?????????");
                    setSuspendState.invoke(entity, "2");
                    setSuspendStateName.invoke(entity, "?????????");
                } else {
                    setTaskName.invoke(entity, task.getName());
                    setSuspendState.invoke(entity, "1");
                    setSuspendStateName.invoke(entity, "?????????");
                }
            } else {
                // ????????????????????????
                List<HistoricTaskInstance> list =
                    historyService.createHistoricTaskInstanceQuery().processInstanceId(instanceId).orderByTaskCreateTime().desc().list();
                if (!CollectionUtils.isEmpty(list)) {
                    HistoricTaskInstance lastTask = list.get(0); // ?????????????????????????????????
                    if (StringUtils.isNotBlank(lastTask.getDeleteReason())) {
                        setTaskName.invoke(entity, "?????????");
                    } else {
                        setTaskName.invoke(entity, "?????????");
                    }
                    setTaskId.invoke(entity, "-1"); // ??????????????????????????????id???????????????-1
                } else {
                    // ????????????????????????????????????????????????instanceId?????????????????????
                    setTaskName.invoke(entity, "???????????????");
                    setTaskId.invoke(entity, "-2"); // ?????????????????????????????????????????????????????????
                }
            }
        } else {
            setTaskName.invoke(entity, "?????????");
        }
    }

    @Override
    public TableDataInfo findTodoTasks(TaskVo taskVo) {
        taskVo.setUserId(SecurityUtils.getUsername());
        taskVo.setOffset((taskVo.getPageNum() - 1) * taskVo.getPageSize());
        
        if(StringUtils.isNotEmpty(taskVo.getBusinessNo())) {
            ProcessInstance instance = runtimeService.createProcessInstanceQuery().variableValueEquals(BUSINESS_NO, taskVo.getBusinessNo()).singleResult();
            if(instance != null) {
                taskVo.setInstanceId(instance.getId());
            }else {
                taskVo.setInstanceId("undefined");
            }
         }
        
        List<Map> tasks = taskMapper.findTodoList(taskVo);
        Integer count = taskMapper.findTodoCount(taskVo);

        List<TaskVo> taskVos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(tasks)) {
            tasks.forEach(task -> {
                TaskVo newTaskVo = new TaskVo();
                newTaskVo.setType("todo");
                newTaskVo.setUserId(SecurityUtils.getUsername());
                newTaskVo.setTaskId(task.get("ID_").toString());
                newTaskVo.setTaskName(task.get("NAME_").toString());
                newTaskVo.setInstanceId(task.get("PROC_INST_ID_").toString());
                newTaskVo.setSuspendState(task.get("SUSPENSION_STATE_").toString());
                newTaskVo.setCreateTime((Date)task.get("CREATE_TIME_"));
                newTaskVo.setTaskDefKey(task.get("TASK_DEF_KEY_").toString());
                if ("2".equals(newTaskVo.getSuspendState())) {
                    newTaskVo.setSuspendStateName("?????????");
                } else {
                    newTaskVo.setSuspendStateName("?????????");
                }
                newTaskVo.setAssigneeName(userMapper.selectUserByUserName(newTaskVo.getUserId()).getNickName());

                // ????????????title
                String title = (String)taskService.getVariable(newTaskVo.getTaskId(), INSTANCE_TITLE);
                newTaskVo.setInstanceTitle(title);
                
                // ??????????????????
                String no = (String)taskService.getVariable(newTaskVo.getTaskId(), BUSINESS_NO);
                newTaskVo.setBusinessNo(no);

                // ????????????key
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(newTaskVo.getInstanceId()).singleResult();
                String key = processInstance.getProcessDefinitionKey();
                newTaskVo.setProcessDefinitionKey(key);

                taskVos.add(newTaskVo);
            });
        }

        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("????????????");
        rspData.setRows(taskVos);
        rspData.setTotal(count);

        return rspData;
    }

    private Map<String, Object> getLine2HumpMap(Map<String, Object> map) {
        Map<String, Object> newMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            // key ?????????????????? apply_user_id ????????? applyUserId
            key = lineToHump(key).substring(0, 1).toLowerCase() + lineToHump(key).substring(1);
            newMap.put(key, value);
        }
        return newMap;
    }

    @Override
    public TableDataInfo findDoneTasks(TaskVo taskVo) {
        taskVo.setUserId(SecurityUtils.getUsername());
        taskVo.setOffset((taskVo.getPageNum() - 1) * taskVo.getPageSize());
        
        if(StringUtils.isNotEmpty(taskVo.getBusinessNo())) {
           HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery().variableValueEquals(BUSINESS_NO, taskVo.getBusinessNo()).singleResult();
           if(instance != null) {
               taskVo.setInstanceId(instance.getId());
           }else {
               taskVo.setInstanceId("undefined");
           }
        }
        
        List<Map> tasks = taskMapper.findDoneList(taskVo);
        Integer count = taskMapper.findDoneCount(taskVo);

        List<TaskVo> taskVos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(tasks)) {
            tasks.forEach(task -> {
                TaskVo newTaskVo = new TaskVo();
                newTaskVo.setType("done");
                newTaskVo.setUserId(SecurityUtils.getUsername());
                newTaskVo.setTaskId(task.get("ID_").toString());
                newTaskVo.setTaskName(task.get("NAME_").toString());
                newTaskVo.setInstanceId(task.get("PROC_INST_ID_").toString());
                newTaskVo.setAssignee(task.get("ASSIGNEE_").toString());
                LocalDateTime startTime = (LocalDateTime)task.get("START_TIME_");
                LocalDateTime endTime = (LocalDateTime)task.get("END_TIME_");
                newTaskVo.setStartTime(DateUtils.localDateTimeToDate(startTime));
                newTaskVo.setEndTime(DateUtils.localDateTimeToDate(endTime));
                newTaskVo.setAssigneeName(userMapper.selectUserByUserName(newTaskVo.getAssignee()).getNickName());

                // ????????????title
                String title = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(newTaskVo.getInstanceId())
                    .variableName(INSTANCE_TITLE)
                    .excludeTaskVariables().singleResult().getValue().toString();
                newTaskVo.setInstanceTitle(title);
                
                // ??????????????????
                String no = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(newTaskVo.getInstanceId())
                    .variableName(BUSINESS_NO)
                    .excludeTaskVariables().singleResult().getValue().toString();
                newTaskVo.setBusinessNo(no);
                
                // ????????????key
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(newTaskVo.getInstanceId()).singleResult();
                String key = "";
                if(processInstance == null) {
                    HistoricProcessInstance historicProcessInstance =
                        historyService.createHistoricProcessInstanceQuery().processInstanceId(newTaskVo.getInstanceId()).singleResult();
                    key = historicProcessInstance.getProcessDefinitionKey();
                }else {
                    key = processInstance.getProcessDefinitionKey();
                }
                newTaskVo.setProcessDefinitionKey(key);
                taskVos.add(newTaskVo);
            });
        }

        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("????????????");
        rspData.setRows(taskVos);
        rspData.setTotal(count);

        return rspData;
    }

    @Override
    public void complete(String taskId, String instanceId, String variablesStr) {
        Map<String, Object> variables = (Map<String, Object>)JSON.parse(variablesStr);
        String comment = variables.get("comment").toString();
        String pass = variables.get("pass").toString();
        try {
            variables.put("pass", "true".equals(pass));
            // ??????????????????????????????
            // p.s. ??????????????????????????? resolved ????????????????????????
            // ????????? complete ??????????????? resolved

            // ????????????????????????????????????????????????
            TaskEntityImpl task = (TaskEntityImpl)taskService.createTaskQuery().taskId(taskId).singleResult();
            // DELEGATION_ ??? PENDING ??????????????????????????????
            if (task.getDelegationState() != null && task.getDelegationState().equals(DelegationState.PENDING)) {
                taskService.resolveTask(taskId, variables);
                // ?????????????????????
                String delegateUserName = userMapper.selectUserByUserName(SecurityUtils.getUsername()).getNickName();
                comment += "??????" + delegateUserName + "?????????";

                // ????????? OWNER_ ??? null ??????????????????????????????????????????????????????????????????????????????
                if (StringUtils.isBlank(task.getOwner())) {
                    taskService.claim(taskId, SecurityUtils.getUsername());
                }
            } else {
                // ?????????????????????act_hi_taskinst ?????? assignee ??????????????? null
                taskService.claim(taskId, SecurityUtils.getUsername());
            }

            if (StringUtils.isNotEmpty(comment)) {
                identityService.setAuthenticatedUserId(SecurityUtils.getUsername());
                taskService.addComment(taskId, instanceId, comment);
            }

            taskService.complete(taskId, variables);
        } catch (Exception e) {
            logger.error("error on complete task {}, variables={}", new Object[] {taskId, variables, e});
        }
    }

    @Override
    public List<HistoricActivity> selectHistoryList(HistoricActivity historicActivity) {
        // ?????????????????????????????????????????? ???????????? ??? ???????????? ???????????????????????????????????????
        // PageDomain pageDomain = TableSupport.buildPageRequest();
        // Integer pageNum = pageDomain.getPageNum();
        // Integer pageSize = pageDomain.getPageSize();
        List<HistoricActivity> activityList = new ArrayList<>();
        HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();
        if (StringUtils.isNotBlank(historicActivity.getAssignee())) {
            query.taskAssignee(historicActivity.getAssignee());
        }
        if (StringUtils.isNotBlank(historicActivity.getActivityName())) {
            query.activityName(historicActivity.getActivityName());
        }
        List<HistoricActivityInstance> list = query.processInstanceId(historicActivity.getProcessInstanceId()).activityType("userTask").finished()
            .orderByHistoricActivityInstanceStartTime().asc().list();
        // .listPage((pageNum - 1) * pageSize, pageNum * pageSize);
        list.forEach(instance -> {
            HistoricActivity activity = new HistoricActivity();
            BeanUtils.copyProperties(instance, activity);
            String taskId = instance.getTaskId();
            List<Comment> comment = taskService.getTaskComments(taskId, "comment");
            if (!CollectionUtils.isEmpty(comment)) {
                activity.setComment(comment.get(0).getFullMessage());
            }
            // ??????????????????deleteReason ?????? null???????????????????????????
            if (StringUtils.isNotBlank(activity.getDeleteReason())) {
                activity.setComment(activity.getDeleteReason());
            }
            SysUser sysUser = userMapper.selectUserByUserName(instance.getAssignee());
            if (sysUser != null) {
                activity.setAssigneeName(sysUser.getNickName());
            }
            activityList.add(activity);
        });

        // ??????????????????????????????????????????
        HistoricActivity startActivity = new HistoricActivity();
        query = historyService.createHistoricActivityInstanceQuery();
        HistoricActivityInstance startActivityInstance =
            query.processInstanceId(historicActivity.getProcessInstanceId()).activityType("startEvent").singleResult();
        BeanUtils.copyProperties(startActivityInstance, startActivity);
        HistoricProcessInstance historicProcessInstance =
            historyService.createHistoricProcessInstanceQuery().processInstanceId(historicActivity.getProcessInstanceId()).singleResult();
        startActivity.setAssignee(historicProcessInstance.getStartUserId());
        SysUser sysUser = userMapper.selectUserByUserName(historicProcessInstance.getStartUserId());
        if (sysUser != null) {
            startActivity.setAssigneeName(sysUser.getNickName());
        }
        startActivity.setComment("????????????");

        // ?????????????????????????????????
        boolean necessaryAdd = true;
        if ((StringUtils.isNotBlank(historicActivity.getActivityName()) && !startActivity.getActivityName().equals(historicActivity.getActivityName()))
            || (StringUtils.isNotBlank(historicActivity.getAssignee()) && !startActivity.getAssignee().equals(historicActivity.getAssignee()))) {
            necessaryAdd = false;
        }
        if (necessaryAdd) {
            activityList.add(0, startActivity);
        }

        // ???????????????????????????????????????
        HistoricActivity endActivity = new HistoricActivity();
        query = historyService.createHistoricActivityInstanceQuery();
        HistoricActivityInstance endActivityInstance = query.processInstanceId(historicActivity.getProcessInstanceId()).activityType("endEvent").singleResult();
        if (null != endActivityInstance) {
            BeanUtils.copyProperties(endActivityInstance, endActivity);
            endActivity.setAssignee("admin");
            sysUser = userMapper.selectUserByUserName("admin");
            if (sysUser != null) {
                endActivity.setAssigneeName(sysUser.getNickName());
            }
            endActivity.setComment("????????????");

            // ?????????????????????????????????
            necessaryAdd = true;
            if ((StringUtils.isNotBlank(historicActivity.getActivityName()) && !endActivity.getActivityName().equals(historicActivity.getActivityName()))
                || (StringUtils.isNotBlank(historicActivity.getAssignee()) && !endActivity.getAssignee().equals(historicActivity.getAssignee()))) {
                necessaryAdd = false;
            }
            if (necessaryAdd) {
                activityList.add(endActivity);
            }
        }

        return activityList;
    }

    @Override
    public void delegate(String taskId, String fromUser, String delegateToUser) {
        taskService.delegateTask(taskId, delegateToUser);
    }

    @Override
    @Transactional
    public void cancelApply(String instanceId, String deleteReason) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        applicationEventPublisher.publishEvent(new WorkflowEvent(this, processInstance, WorkflowEvent.EVENT_CANCEL_APPLY));
        // ???????????????????????????????????? act_ru_task ??????????????????????????? act_hi_taskinst ????????????????????????????????????????????????finished??????
        runtimeService.deleteProcessInstance(instanceId, deleteReason);
    }

    @Override
    public void suspendOrActiveApply(String instanceId, String suspendState) {
        if ("1".equals(suspendState)) {
            // ????????????????????????????????????????????????????????????????????????id??????????????????????????????
            // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            // ??????????????????????????????????????????????????????????????????????????????
            // ???????????????act_ru_task ??? SUSPENSION_STATE_ ??? 2
            runtimeService.suspendProcessInstanceById(instanceId);
        } else if ("2".equals(suspendState)) {
            runtimeService.activateProcessInstanceById(instanceId);
        }
    }

    @Override
    public TableDataInfo findTaskApplyedByMe(MyApplyVo myApplyVo) {
        String username = SecurityUtils.getUsername();
        myApplyVo.setOffset((myApplyVo.getPageNum() - 1) * myApplyVo.getPageSize());
        HistoricProcessInstanceQuery  query = historyService.createHistoricProcessInstanceQuery().startedBy(username);
        
        if(StringUtils.isNotEmpty(myApplyVo.getProcInstName())) {
            query.processInstanceNameLike("%"+myApplyVo.getProcInstName()+"%");
        }
        if(myApplyVo.getStartTimeBegin() != null && myApplyVo.getStartTimeEnd() != null) {
            query.startedBefore(myApplyVo.getStartTimeEnd());
            query.startedAfter(myApplyVo.getStartTimeBegin());
        }
        
        List<HistoricProcessInstance> process = query.listPage(myApplyVo.getOffset(), myApplyVo.getPageSize());
        Long count = query.count();
        
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("????????????");
        rspData.setRows(process);
        rspData.setTotal(count);

        return rspData;
    }

}
