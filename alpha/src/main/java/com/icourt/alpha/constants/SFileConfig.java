package com.icourt.alpha.constants;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.icourt.alpha.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import static com.icourt.alpha.constants.Const.VIEW_TYPE_GRID;
import static com.icourt.alpha.constants.Const.VIEW_TYPE_ITEM;

/**
 * Description
 * Company Beijing icourt
 * author  youxuan  E-mail:xuanyouwu@163.com
 * date createTime：2017/8/21
 * version 2.1.0
 */
public class SFileConfig {

    /**
     * 0： "我的资料库",
     * 1： "共享给我的",
     * 2： "律所资料库",
     * 3： "项目资料库"
     */
    public static final int REPO_MINE = 0;
    public static final int REPO_SHARED_ME = 1;
    public static final int REPO_LAWFIRM = 2;
    public static final int REPO_PROJECT = 3;
    public static final int REPO_UNKNOW = -1;//不明确类型的
    public static Map<String, Integer> resourcesDocumentIcon = new HashMap<String, Integer>() {
        {
            put("doc", R.mipmap.filetype_doc);
            put("wps", R.mipmap.filetype_doc);
            put("rtf", R.mipmap.filetype_doc);
            put("docx", R.mipmap.filetype_doc);

            put("jpg", R.mipmap.filetype_image);
            put("jpeg", R.mipmap.filetype_image);
            put("png", R.mipmap.filetype_image);
            put("gif", R.mipmap.filetype_image);
            put("pic", R.mipmap.filetype_image);

            put("pdf", R.mipmap.filetype_pdf);
            put("ppt", R.mipmap.filetype_ppt);
            put("pptx", R.mipmap.filetype_ppt);

            put("numbers", R.mipmap.filetype_number);
            put("pages", R.mipmap.filetype_pages);
            put("key", R.mipmap.filetype_keynote);

            put("xls", R.mipmap.filetype_excel);
            put("xlsx", R.mipmap.filetype_excel);
            put("xlsm", R.mipmap.filetype_excel);

            put("zip", R.mipmap.filetype_zip);
            put("rar", R.mipmap.filetype_zip);
            put("apk", R.mipmap.filetype_zip);

            put("mp3", R.mipmap.filetype_music);
            put("wav", R.mipmap.filetype_music);

            put("mp4", R.mipmap.filetype_video);
            put("avi", R.mipmap.filetype_video);
            put("ram", R.mipmap.filetype_video);
            put("rm", R.mipmap.filetype_video);
            put("mpg", R.mipmap.filetype_video);
            put("mpeg", R.mipmap.filetype_video);
            put("wmv", R.mipmap.filetype_video);

            put("httpd/unix-directory", R.mipmap.folder);
        }
    };

    @IntDef({REPO_MINE,
            REPO_SHARED_ME,
            REPO_LAWFIRM,
            REPO_PROJECT,
            REPO_UNKNOW})
    @Retention(RetentionPolicy.SOURCE)
    public @interface REPO_TYPE {

    }

    /**
     * 转换
     *
     * @param repoType
     * @return
     */
    @REPO_TYPE
    public static final int convert2RepoType(int repoType) {
        switch (repoType) {
            case REPO_MINE:
                return REPO_MINE;
            case REPO_SHARED_ME:
                return REPO_SHARED_ME;
            case REPO_LAWFIRM:
                return REPO_LAWFIRM;
            case REPO_PROJECT:
                return REPO_PROJECT;
            case REPO_UNKNOW:
                return REPO_UNKNOW;
            default:
                return REPO_UNKNOW;
        }
    }


    /**
     * 文件权限
     */
    public static final String PERMISSION_RW = "rw";
    public static final String PERMISSION_R = "r";

    @StringDef({PERMISSION_RW,
            PERMISSION_R})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FILE_PERMISSION {

    }

    /**
     * 转换
     *
     * @param permission
     * @return
     */
    @FILE_PERMISSION
    public static final String convert2filePermission(String permission) {
        String stringPermission = permission;
        if (TextUtils.equals(stringPermission, PERMISSION_RW)) {
            return PERMISSION_RW;
        } else if (TextUtils.equals(stringPermission, PERMISSION_R)) {
            return PERMISSION_R;
        } else {
            return PERMISSION_R;
        }
    }

    /**
     * sfile列表全局记录展现样式 仅仅限于内存保存
     */
    public static final ArrayMap<String, Integer> SFileLayoutTypeMap = new ArrayMap<>();

    /**
     * @param repoId
     * @return
     */
    @Const.AdapterViewType
    public static int getSFileLayoutType(String repoId, @Const.AdapterViewType int defaultType) {
        Integer integer = SFileLayoutTypeMap.get(repoId);
        if (integer != null) {
            switch (integer.intValue()) {
                case VIEW_TYPE_ITEM:
                    return VIEW_TYPE_ITEM;
                case VIEW_TYPE_GRID:
                    return VIEW_TYPE_GRID;
            }
        }
        return defaultType;
    }

    /**
     * 保存
     *
     * @param repoId
     * @param layoutType
     * @return
     */
    public static Integer putSFileLayoutType(String repoId, @Const.AdapterViewType int layoutType) {
        return SFileLayoutTypeMap.put(repoId, layoutType);
    }

    public static final int FILE_FROM_TASK = 1;         //任务下附件
    public static final int FILE_FROM_PROJECT = 2;      //项目下载附件
    public static final int FILE_FROM_REPO = 4;         //资料库下载附件
    public static final int FILE_FROM_IM = 3;           //享聊下载附件


    @IntDef({SFileConfig.FILE_FROM_TASK,
            SFileConfig.FILE_FROM_PROJECT,
            SFileConfig.FILE_FROM_IM,
            SFileConfig.FILE_FROM_REPO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FILE_FROM {
    }

    @FILE_FROM
    public static final int convert2FileFrom(int fileFrom) {
        switch (fileFrom) {
            case FILE_FROM_TASK:
                return FILE_FROM_TASK;
            case FILE_FROM_PROJECT:
                return FILE_FROM_PROJECT;
            case FILE_FROM_IM:
                return FILE_FROM_IM;
            case FILE_FROM_REPO:
                return FILE_FROM_REPO;
            default:
                return FILE_FROM_REPO;
        }
    }

    /**
     * seafile 最大长度
     */
    public static final int SFILE_FILE_NAME_MAX_LENGTH = 80;
}
