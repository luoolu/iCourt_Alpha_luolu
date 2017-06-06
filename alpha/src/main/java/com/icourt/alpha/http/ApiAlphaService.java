package com.icourt.alpha.http;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icourt.alpha.entity.bean.AlphaUserInfo;
import com.icourt.alpha.entity.bean.AppVersionEntity;
import com.icourt.alpha.entity.bean.CommentEntity;
import com.icourt.alpha.entity.bean.ContactDeatilBean;
import com.icourt.alpha.entity.bean.CustomerEntity;
import com.icourt.alpha.entity.bean.FileBoxBean;
import com.icourt.alpha.entity.bean.GroupBean;
import com.icourt.alpha.entity.bean.GroupContactBean;
import com.icourt.alpha.entity.bean.IMMessageCustomBody;
import com.icourt.alpha.entity.bean.ItemPageEntity;
import com.icourt.alpha.entity.bean.LoginIMToken;
import com.icourt.alpha.entity.bean.MsgConvert2Task;
import com.icourt.alpha.entity.bean.PageEntity;
import com.icourt.alpha.entity.bean.ProjectDetailEntity;
import com.icourt.alpha.entity.bean.ProjectEntity;
import com.icourt.alpha.entity.bean.SearchEngineEntity;
import com.icourt.alpha.entity.bean.SelectGroupBean;
import com.icourt.alpha.entity.bean.TaskAttachmentEntity;
import com.icourt.alpha.entity.bean.TaskCheckItemEntity;
import com.icourt.alpha.entity.bean.TaskEntity;
import com.icourt.alpha.entity.bean.TaskGroupEntity;
import com.icourt.alpha.entity.bean.TaskMemberWrapEntity;
import com.icourt.alpha.entity.bean.TaskOwerEntity;
import com.icourt.alpha.entity.bean.TimeEntity;
import com.icourt.alpha.entity.bean.TimingCountEntity;
import com.icourt.alpha.entity.bean.UserDataEntity;
import com.icourt.alpha.entity.bean.WorkType;
import com.icourt.alpha.http.httpmodel.ResEntity;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * @author xuanyouwu
 * @email xuanyouwu@163.com
 * @time 2016-06-02 14:26
 * <p>
 * 分页公共参数 整形  请大家按照这个【顺序】写
 * @Query("pageNum") int pageNum,
 * @Query("pageSize") int pageSize
 */
public interface ApiAlphaService {


    /**
     * 获取新版本app
     * 文档参考 https://fir.im/docs/version_detection
     *
     * @param url fir地址
     * @return
     */
    @GET
    Call<AppVersionEntity> getNewVersionAppInfo(
            @Url String url);

    /**
     * 修改律师电话信息
     *
     * @param phone 手机号码 不包含+86国际代码的字符串
     * @return
     */
    @Deprecated
    @POST("api/v1/auth/update")
    @FormUrlEncoded
    Call<ResEntity<String>> updateUserPhone(@Field("phone") String phone);

    /**
     * 修改律师邮箱信息
     *
     * @param email
     * @return
     */
    @Deprecated
    @POST("api/v1/auth/update")
    @FormUrlEncoded
    Call<ResEntity<String>> updateUserEmail(@Field("email") String email);

    /**
     * 微信登陆
     * <p>
     * 将"opneid" "unionid" "uniqueDevice"="device"; "deviceType"="android" 组合成json
     *
     * @return
     */
    @POST("v2/weixinlogin/getTokenByOpenidAndUnionid")
    Call<ResEntity<AlphaUserInfo>> loginWithWeiXin(@Body RequestBody info);

    /**
     * 账号密码登陆
     *
     * @param info json请求体
     * @return
     */
    @POST("api/v1/auth/login")
    Call<AlphaUserInfo> loginWithPwd(@Body RequestBody info);

    /**
     * 获取云信登陆的token
     *
     * @return
     */
    @GET("api/v2/chat/msg/token")
    Call<ResEntity<LoginIMToken>> getChatToken();

    /**
     * 刷新登陆refreshToken过时
     * 注意请求的key是 refreshToekn
     * 注意这个api 不支持post
     *
     * @param refreshToken 已经登陆的refreshToken
     * @return
     */
    @GET("api/v1/auth/refresh")
    Call<ResEntity<AlphaUserInfo>> refreshToken(@Query("refreshToekn") String refreshToken);

    /**
     * 获取团队联系人列表
     *
     * @param officeId 在登陆信息中有
     * @return
     */
    @Deprecated
    @GET("api/v1/auth/q/allByOfficeId/{officeId}")
    Call<ResEntity<List<GroupContactBean>>> getGroupContacts(@Path("officeId") String officeId);


    /**
     * 根据不同类型获取文件列表
     *
     * @param type     TYPE_ALL_FILE = 0;  TYPE_MY_FILE = 1;
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GET("api/v2/chat/msg/findFileMsg")
    @Deprecated
    Call<ResEntity<List<IMMessageCustomBody>>> getFilesByType(
            @Query("type") int type,
            @Query("pageNum") int pageNum,
            @Query("pageSize") int pageSize
    );


    /**
     * 获取搜索引擎列表
     */
    @GET("api/v2/site/getSiteList")
    Call<ResEntity<List<SearchEngineEntity>>> getSearchEngines();

    /**
     * 获取客户列表
     *
     * @param pagesize
     * @return
     */
    @GET("api/v2/contact")
    Call<ResEntity<List<CustomerEntity>>> getCustomers(@Query("pagesize") int pagesize);

    /**
     * 获取客户列表
     *
     * @param pageindex
     * @param pagesize
     * @param isView    是否关注的 关注==1
     * @return
     */
    @GET("api/v2/contact")
    Call<ResEntity<List<CustomerEntity>>> getCustomers(@Query("pageindex") int pageindex,
                                                       @Query("pagesize") int pagesize,
                                                       @Query("isView") int isView);

    /**
     * 获取所有任务
     *
     * @return
     */
    @GET("api/v2/taskflow/queryTaskByDue")
    Call<ResEntity<PageEntity<TaskEntity>>> getAllTask();


    /**
     * 消息转任务
     *
     * @param content
     * @return
     */
    @POST("api/v2/chat/msg/analysisTask")
    @FormUrlEncoded
    Call<ResEntity<MsgConvert2Task>> msgConvert2Task(@Field("content") String content);


    /**
     * 群组文件上传
     *
     * @param groupId
     * @param params
     * @return
     */
    @POST("api/v2/file/upload")
    @Multipart
    Call<ResEntity<JsonElement>> groupUploadFile(@Query("groupId") String groupId,
                                                 @PartMap Map<String, RequestBody> params
    );

    /**
     * 项目列表
     *
     * @param pageindex
     * @param pagesize
     * @param orderby
     * @param ordertype
     * @param status
     * @param matterType
     * @param attorneyType
     * @param myStar
     * @return
     */
    @GET("api/v1/matters")
    Call<ResEntity<List<ProjectEntity>>> projectQueryAll(@Query("pageindex") int pageindex,
                                                         @Query("pagesize") int pagesize,
                                                         @Query("orderby") String orderby,
                                                         @Query("ordertype") String ordertype,
                                                         @Query("status") String status,
                                                         @Query("matterType") String matterType,
                                                         @Query("attorneyType") String attorneyType,
                                                         @Query("myStar") String myStar
    );

    /**
     * 获取选择项目列表
     *
     * @param status 项目状态：[0:预立案 2:进行中 4:已完结 7:已搁置]，多个以英文逗号分隔
     * @return
     */
    @GET("api/v1/matters/keyValue")
    Call<ResEntity<List<ProjectEntity>>> projectSelectListQuery(@Query("status") String status);


    /**
     * 计时项目列表搜索
     *
     * @param myStar
     * @param status
     * @return
     */
    @GET("api/v2/timing/timing/getMatterList")
    Call<ResEntity<List<ProjectEntity>>> timingProjectQuery(
            @Query("myStar") int myStar,
            @Query("status") String status
    );

    /**
     * 计时项目列表搜索
     * pms接口独有
     *
     * @param myStar
     * @param status
     * @return
     */
    @GET("api/v2/timing/timing/getMatterList")
    Call<ResEntity<List<ProjectEntity>>> timingProjectQuery(
            @Query("myStar") int myStar,
            @Query("status") String status,
            @Query("word") String word
    );

    /**
     * 获取项目概览
     *
     * @param id
     * @return
     */
    @GET("api/v1/matters/{id}")
    Call<ResEntity<List<ProjectDetailEntity>>> projectDetail(@Path("id") String id);

    /**
     * 项目添加关注
     *
     * @param matterPkid
     * @return
     */
    @PUT("api/v1/matters/addStar")
    Call<ResEntity<JsonElement>> projectAddStar(@Query("matterPkid") String matterPkid);

    /**
     * 项目取消关注
     *
     * @param matterPkid
     * @return
     */
    @DELETE("api/v1/matters/deleteStar")
    Call<ResEntity<JsonElement>> projectDeleteStar(@Query("matterPkid") String matterPkid);

    /**
     * 更新用户信息
     *
     * @param id
     * @param phone
     * @param email
     * @return
     */
    @POST("api/v1/auth/update")
    Call<ResEntity<JsonElement>> updateUserInfo(@Query("id") String id, @Query("phone") String phone, @Query("email") String email);

    /**
     * 项目下计时列表
     *
     * @param matterId
     * @param pageSize
     * @return
     */
    @GET("api/v2/timing/timing/findByMatterId")
    Call<ResEntity<TimeEntity>> projectQueryTimerList(@Query("matterId") String matterId, @Query("pageIndex") int pageIndex, @Query("pageSize") int pageSize);

    /**
     * 获取项目详情文档列表token
     *
     * @return
     */
    @GET("api/v2/documents/getToken")
    Call<JsonObject> projectQueryFileBoxToken();

    /**
     * 获取项目详情文档id
     *
     * @param projectId
     * @return
     */
    @GET("api/v2/documents/getRepo/{projectId}")
    Call<JsonObject> projectQueryDocumentId(@Path("projectId") String projectId);

    /**
     * 获取项目详情文档列表
     *
     * @param authToken
     * @param seaFileRepoId
     * @return
     */
    @GET("https://box.alphalawyer.cn/api2/repos/{seaFileRepoId}/dir/")
    Call<List<FileBoxBean>> projectQueryFileBoxList(@Header("Authorization") String authToken, @Path("seaFileRepoId") String seaFileRepoId);

    /**
     * 项目下任务列表
     *
     * @param projectId
     * @param stateType 全部任务:－1    已完成:1     未完成:0
     * @param type      任务和任务组：-1;    任务：0;    任务组：1;
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @GET("api/v2/taskflow/queryMatterTask")
    Call<ResEntity<TaskEntity>> projectQueryTaskList(@Query("matterId") String projectId,
                                                     @Query("stateType") int stateType,
                                                     @Query("type") int type,
                                                     @Query("pageIndex") int pageIndex,
                                                     @Query("pageSize") int pageSize);

    /**
     * 项目下任务组列表
     *
     * @param projectId
     * @return
     */
    @GET("api/v2/flowmatter/flowbyMatterId")
    Call<ResEntity<List<TaskGroupEntity>>> projectQueryTaskGroupList(@Query("matterId") String projectId);

    /**
     * 新建任务组
     *
     * @param msg
     * @return
     */
    @POST("api/v2/taskflow")
    Call<ResEntity<TaskGroupEntity>> taskGroupCreate(@Body RequestBody msg);

    /**
     * 修改任务
     *
     * @param msg
     * @return
     */
    @PUT("api/v2/taskflow")
    Call<ResEntity<JsonElement>> taskUpdate(@Body RequestBody msg);

    /**
     * 获取任务详情
     *
     * @param id
     * @return
     */
    @GET("api/v2/taskflow/{id}")
    Call<ResEntity<TaskEntity.TaskItemEntity>> taskQueryDetail(@Path("id") String id);

    /**
     * 更新计时
     *
     * @return
     */
    @PUT("api/v2/timing/timing/update")
    Call<ResEntity<JsonElement>> timingUpdate(@Body RequestBody body);

    /**
     * 新建计时
     *
     * @param body
     * @return
     */
    @POST("api/v2/timing/timing/add")
    Call<ResEntity<String>> timingAdd(@Body RequestBody body);

    /**
     * 获取任务下检查项列表
     *
     * @param taskId
     * @return
     */
    @GET("api/v2/taskflow/taskitem")
    Call<ResEntity<TaskCheckItemEntity>> taskCheckItemQuery(@Query("taskId") String taskId);

    /**
     * 修改任务下检查项
     *
     * @param body
     * @return
     */
    @PUT("api/v2/taskflow/taskitem")
    Call<ResEntity<JsonElement>> taskCheckItemUpdate(@Body RequestBody body);

    /**
     * 删除任务下检查项
     *
     * @param id
     * @return
     */
    @DELETE("api/v2/taskflow/taskitem/{id}")
    Call<ResEntity<JsonElement>> taskCheckItemDelete(@Path("id") String id);

    /**
     * 添加任务下检查项
     *
     * @param body
     * @return
     */
    @POST("api/v2/taskflow/taskitem")
    Call<ResEntity<JsonElement>> taskCheckItemCreate(@Body RequestBody body);

    /**
     * 任务添加关注
     *
     * @param body
     * @return
     */
    @POST("api/v2/taskflow/attention")
    Call<ResEntity<JsonElement>> taskAddStar(@Body RequestBody body);

    /**
     * 任务取消关注
     *
     * @param id
     * @return
     */
    @DELETE("api/v2/taskflow/attention/{id}")
    Call<ResEntity<JsonElement>> taskDeleteStar(@Path("id") String id);

    /**
     * 删除任务
     *
     * @param id
     * @return
     */
    @DELETE("api/v2/taskflow/{id}")
    Call<ResEntity<JsonElement>> taskDelete(@Path("id") String id);

    /**
     * 任务添加评论
     *
     * @param hostType 被评论的对象类型:100为任务
     * @param hostId   被评论的对象id
     * @param content  评论的内容
     * @return
     */
    @POST("api/v2/comment")
    Call<ResEntity<JsonElement>> commentCreate(@Query("hostType") int hostType,
                                               @Query("hostId") String hostId,
                                               @Query("content") String content);

    /**
     * 获取评论列表
     *
     * @param hostType  被评论的对象类型:100为任务
     * @param hostId    被评论的对象id
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @GET("api/v2/comment")
    Call<ResEntity<CommentEntity>> commentListQuery(@Query("hostType") int hostType,
                                                    @Query("hostId") String hostId,
                                                    @Query("pageIndex") int pageIndex,
                                                    @Query("pageSize") int pageSize);

    /**
     * 任务列表
     *
     * @param assignedByMe  0：所有； 1：我分配的
     * @param assignTos     分配给谁的，用户的id序列
     * @param attentionType 全部:0    我关注的:1
     * @param orderBy       按指定类型排序或分组；matterId表示按项目排序;createTime表示按日期排序(默认);parentId表示按清单;assignTo表示按负责人排序
     * @param stateType     全部任务:－1    已完成:1     未完成:0
     * @param type          任务和任务组：-1;    任务：0;    任务组：1;
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @GET("api/v2/taskflow")
    Call<ResEntity<TaskEntity>> taskListQuery(@Query("assignedByMe") int assignedByMe,
                                              @Query("assignTos") String assignTos,
                                              @Query("stateType") int stateType,
                                              @Query("attentionType") int attentionType,
                                              @Query("orderBy") String orderBy,
                                              @Query("pageIndex") int pageIndex,
                                              @Query("pageSize") int pageSize,
                                              @Query("type") int type);

    /**
     * 任务列表
     *
     * @param assignedByMe  0：所有； 1：我分配的
     * @param assignTos     分配给谁的，用户的id序列
     * @param attentionType 全部:0    我关注的:1
     * @param orderBy       按指定类型排序或分组；matterId表示按项目排序;createTime表示按日期排序(默认);parentId表示按清单;assignTo表示按负责人排序
     * @param stateType     全部任务:－1    已完成:1     未完成:0
     * @param type          任务和任务组：-1;    任务：0;    任务组：1;
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @GET("api/v2/taskflow")
    Call<ResEntity<TaskEntity>> taskListItemQuery(@Query("assignedByMe") int assignedByMe,
                                                  @Query("assignTos") String assignTos,
                                                  @Query("stateType") int stateType,
                                                  @Query("attentionType") int attentionType,
                                                  @Query("orderBy") String orderBy,
                                                  @Query("pageIndex") int pageIndex,
                                                  @Query("pageSize") int pageSize,
                                                  @Query("type") int type);

    /**
     * 项目下任务列表
     *
     * @param stateType
     * @param matterId
     * @param type
     * @return
     */
    @GET("api/v2/taskflow")
    Call<ResEntity<TaskEntity>> taskListQueryByMatterId(
            @Query("stateType") int stateType,
            @Query("matterId") String matterId,
            @Query("type") int type);

    /**
     * 获取任务下的附件列表
     *
     * @param taskId
     * @return
     */
    @GET("api/v2/task/{taskId}/attachments")
    Call<ResEntity<List<TaskAttachmentEntity>>> taskAttachMentListQuery(@Path("taskId") String taskId);

    /**
     * 任务上传附件
     *
     * @param taskId
     * @param params
     * @return
     */
    @Multipart
    @POST("api/v2/task/{taskId}/attachment/addFromFile")
    Call<ResEntity<JsonElement>> taskAttachmentUpload(@Path("taskId") String taskId, @PartMap Map<String, RequestBody> params);

    /**
     * 获取指定时间段的计时
     *
     * @param createUserId
     * @param startTime    017-05-09
     * @param endTime      017-05-15
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @GET("api/v2/timing/timing/search")
    Call<ResEntity<TimeEntity>> timingListQueryByTime(@Query("createUserId") String createUserId,
                                                      @Query("startTime") String startTime,
                                                      @Query("endTime") String endTime,
                                                      @Query("pageIndex") int pageIndex,
                                                      @Query("pageSize") int pageSize);

    /**
     * 获取上传文件url
     *
     * @param seaFileRepoId
     * @return
     */
    @GET("https://box.alphalawyer.cn/api2/repos/{seaFileRepoId}/upload-link/")
    Call<JsonElement> projectUploadUrlQuery(@Header("Authorization") String authToken, @Path("seaFileRepoId") String seaFileRepoId);


    /**
     * 获取指定时间段的计时统计
     *
     * @param workStartDate 2015-05-03
     * @param workEndDate   2015-05-10
     * @return
     */
    @GET("api/v2/timing/timing/timingCountByTime")
    Call<ResEntity<ItemPageEntity<TimingCountEntity>>> queryTimingCountByTime(@Query("workStartDate") String workStartDate,
                                                                              @Query("workEndDate") String workEndDate);

    /**
     * 项目下上传文件
     *
     * @param authToken
     * @param url
     * @param params
     * @return
     */
    @Multipart
    @POST()
    Call<JsonElement> projectUploadFile(@Header("Authorization") String authToken,
                                        @Url String url,
                                        @PartMap Map<String, RequestBody> params);

    /**
     * 获取项目下文档列表
     *
     * @param authToken
     * @param seaFileRepoId
     * @param rootName
     * @return
     */
    @GET("https://box.alphalawyer.cn/api2/repos/{seaFileRepoId}/dir/")
    Call<List<FileBoxBean>> projectQueryFileBoxByDir(@Header("Authorization") String authToken, @Path("seaFileRepoId") String seaFileRepoId, @Query("p") String rootName);

    /**
     * 获取文件下载地址
     *
     * @param seaFileRepoId
     * @param rootName
     * @return
     */
    @GET("https://box.alphalawyer.cn/api2/repos/{seaFileRepoId}/file/")
    Call<JsonElement> fileboxDownloadUrlQuery(@Header("Authorization") String authToken, @Path("seaFileRepoId") String seaFileRepoId, @Query("p") String rootName);

    /**
     * 下载文件
     *
     * @param authToken
     * @param url
     * @return
     */
    @GET()
    Call<JsonElement> fileboxDownload(@Header("Authorization") String authToken, @Url String url);

    /**
     * 获取项目下的工作类型
     *
     * @param matterId
     * @return
     */
    @GET("api/v2/timing/workTypes")
    Call<ResEntity<List<WorkType>>> queryWorkTypes(@Query("matterId") String matterId);


    /**
     * 获取项目参与人
     *
     * @param project
     * @return
     */
    @GET("api/v1/matters/attorney")
    Call<ResEntity<List<TaskOwerEntity>>> taskOwerListQuery(@Query("id") String project);


    /**
     * 删除计时
     *
     * @param timerId
     * @return
     */
    @DELETE("api/v2/timing/timing/delete/{timerId}")
    Call<ResEntity<JsonElement>> timingDelete(@Path("timerId") String timerId);

    /**
     * 新建任务
     *
     * @param body
     * @return
     */
    @POST("api/v2/taskflow")
    Call<ResEntity<JsonElement>> taskCreate(@Body RequestBody body);


    /**
     * 计时查询
     *
     * @param pageIndex
     * @param pageSize
     * @param state
     * @return
     */
    @GET("api/v2/timing/timing/search")
    Call<ResEntity<PageEntity<TimeEntity.ItemEntity>>> timerQuery(@Query("pageIndex") int pageIndex,
                                                                  @Query("pageSize") int pageSize,
                                                                  @Query("state") int state);

    /**
     * 获得token里律师所属的团队信息
     *
     * @return
     */
    @GET("api/v1/auth/groups/q/groupByToken")
    Call<ResEntity<List<GroupBean>>> lawyerGroupListQuery();

    /**
     * 获取联系人详情
     *
     * @param id
     * @return
     */
    @GET("api/v2/contact/detail/{id}")
    Call<ResEntity<List<ContactDeatilBean>>> customerDetailQuery(@Path("id") String id);

    /**
     * 获取企业联络人
     *
     * @param id
     * @return
     */
    @GET("api/v2/contact/relatedperson/{id}")
    Call<ResEntity<List<ContactDeatilBean>>> customerLiaisonsQuery(@Path("id") String id);

    /**
     * 联系人添加关注
     *
     * @param id
     * @return
     */
    @PUT("api/v2/contact/addStar/{id}")
    Call<ResEntity<JsonElement>> customerAddStar(@Path("id") String id);

    /**
     * 联系人删除关注
     *
     * @param id
     * @return
     */
    @DELETE("api/v2/contact/deleteStar/{id}")
    Call<ResEntity<JsonElement>> customerDeleteStar(@Path("id") String id);

    /**
     * 获取所在律所中的团队列表
     *
     * @param id
     * @return
     */
    @GET("api/v1/auth/groups/up/q/office/{id}")
    Call<ResEntity<List<SelectGroupBean>>> officeGroupsQuery(@Path("id") String id);

    /**
     * 修改联系人所属团队
     *
     * @param body
     * @return
     */
    @PUT("api/v2/contact/group")
    Call<ResEntity<JsonElement>> customerGroupInfoUpdate(@Body RequestBody body);

    /**
     * 获取联系人的联络人
     *
     * @param id
     * @return
     */
    @GET("api/v2/contact/relatedperson/{id}")
    Call<ResEntity<List<ContactDeatilBean>>> liaisonsQuery(@Path("id") String id);

    /**
     * 修改联系人
     *
     * @param body
     * @return
     */
    @PUT("api/v2/contact/mobile")
    Call<ResEntity<List<ContactDeatilBean>>> customerUpdate(@Body RequestBody body);

    /**
     * 添加联系人
     *
     * @param body
     * @return
     */
    @POST("api/v2/contact")
    Call<ResEntity<List<ContactDeatilBean>>> customerCreate(@Body RequestBody body);

    /**
     * 检测企业联系人是否重复
     *
     * @param accuratename
     * @return
     */
    @GET("api/v2/contact")
    Call<ResEntity<JsonObject>> companyCheckReName(@Query("accuratename") String accuratename);


    /**
     * 添加企业联系人
     *
     * @param body
     * @return
     */
    @POST("api/v2/contact/company")
    Call<ResEntity<List<ContactDeatilBean>>> customerCompanyCreate(@Body RequestBody body);


    /**
     * 查看任务的成员 对应的成员(有权限)
     *
     * @return
     */
    @GET("api/v1/auth/get/members")
    Call<ResEntity<List<TaskMemberWrapEntity>>> getPremissionTaskMembers();

    /**
     * 查看任务的成员 对应的成员(无权限)
     *
     * @return
     */
    @GET("api/v1/auth/groups/q/groupByToken")
    Call<ResEntity<List<TaskMemberWrapEntity>>> getUnPremissionTaskMembers();


    /**
     * 获取某人今日计时、本月计时、本月完成任务、本月总任务
     *
     * @param userId
     * @return
     */
    @GET("api/v2/taskflow/getTimingAndTask")
    Call<ResEntity<UserDataEntity>> getUserData(@Query("userId") String userId);


    /**
     * 搜索项目列表
     *
     * @param queryString
     * @param myStar
     * @return
     */
    @GET("api/v1/matters")
    Call<ResEntity<List<ProjectEntity>>> projectQueryByName(@Query("queryString") String queryString,
                                                            @Query("myStar") int myStar);


    /**
     * 搜索任务列表
     *
     * @param assignTos
     * @param name
     * @param stateType 0:未完成；1：已完成；2：已删除
     * @param queryType 0:全部；1：新任务；2：我关注的；3我部门的
     * @return
     */
    @GET("api/v2/taskflow/queryMobileTask")
    Call<ResEntity<TaskEntity>> taskQueryByName(@Query("assignTos") String assignTos,
                                                @Query("name") String name,
                                                @Query("stateType") int stateType,
                                                @Query("queryType") int queryType);


    /**
     * 搜索任务列表
     *
     * @param assignTos
     * @param name
     * @param stateType 0:未完成；1：已完成；2：已删除
     * @param queryType 0:全部；1：新任务；2：我关注的；3我部门的
     * @return
     */
    @GET("api/v2/taskflow/queryMobileTask")
    Call<ResEntity<TaskEntity>> taskQueryByName(@Query("assignTos") String assignTos,
                                                @Query("name") String name,
                                                @Query("stateType") int stateType,
                                                @Query("queryType") int queryType,
                                                @Query("matterId") String matterId);
    /**************************权限模块**************************/
    /**
     * 获取各个模块是否有权限 接口真烂
     *
     * @param userId
     * @param moduleType //MAT,CON,KM,HR,DEP
     * @return
     */
    @GET("api/v2/permission/department/getUserViewModule")
    Call<ResEntity<Boolean>> permissionQuery(@Query("userId") String userId,
                                             @Query("moduleType") String moduleType);

    /**
     * 获取是否有新建任务/联系人查看编辑等权限
     * 聚合权限
     *
     * @param uid
     * @param type      //MAT,CON,KM,HR,DEP
     * @param subjectid
     * @return
     */
    @GET("api/v2/permission/engine/{uid}/getPmsStrings")
    Call<ResEntity<List<String>>> permissionQuery(@Path("uid") String uid,
                                                  @Query("type") String type,
                                                  @Query("subjectid") String subjectid);
}


