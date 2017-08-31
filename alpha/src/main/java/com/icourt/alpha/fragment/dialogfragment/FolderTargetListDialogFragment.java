package com.icourt.alpha.fragment.dialogfragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icourt.alpha.R;
import com.icourt.alpha.base.BaseDialogFragment;
import com.icourt.alpha.constants.Const;
import com.icourt.alpha.entity.bean.FolderDocumentEntity;
import com.icourt.alpha.entity.bean.RepoEntity;
import com.icourt.alpha.fragment.FolderTargetListFragment;
import com.icourt.alpha.fragment.RepoSelectListFragment;
import com.icourt.alpha.http.callback.SFileCallBack;
import com.icourt.alpha.interfaces.OnFragmentCallBackListener;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Response;

import static com.icourt.alpha.constants.Const.FILE_ACTION_COPY;
import static com.icourt.alpha.constants.Const.FILE_ACTION_MOVE;

/**
 * Description  文件移动copy目标路径
 * Company Beijing icourt
 * author  youxuan  E-mail:xuanyouwu@163.com
 * date createTime：2017/8/12
 * version 2.1.0
 */
public class FolderTargetListDialogFragment
        extends BaseDialogFragment
        implements OnFragmentCallBackListener {
    @BindView(R.id.titleBack)
    CheckedTextView titleBack;
    @BindView(R.id.titleContent)
    TextView titleContent;
    @BindView(R.id.foldr_go_parent_tv)
    TextView foldrGoParentTv;
    @BindView(R.id.foldr_parent_tv)
    TextView foldrParentTv;
    Unbinder unbinder;
    @BindView(R.id.main_fl_content)
    FrameLayout mainFlContent;
    protected static final String KEY_SEA_FILE_SELCTED_FILES = "seaFileSelctedFiles";
    protected static final String KEY_SEA_FILE_FROM_REPO_ID = "seaFileFromRepoId";//原仓库id
    protected static final String KEY_SEA_FILE_FROM_DIR_PATH = "seaFileFromDirPath";//原仓库路径

    protected static final String KEY_SEA_FILE_DST_REPO_ID = "seaFileDstRepoId";//目标仓库id
    protected static final String KEY_SEA_FILE_DST_DIR_PATH = "seaFileDstDirPath";//目标仓库路径
    protected static final String KEY_FOLDER_ACTION_TYPE = "folderActionType";//文件操作类型
    @BindView(R.id.dir_path_title_layout)
    RelativeLayout dirPathTitleLayout;

    Fragment currFragment;
    RepoEntity repoEntity;

    public static FolderTargetListDialogFragment newInstance(
            @Const.FILE_ACTION_TYPE int folderActionType,
            String fromRepoId,
            String fromRepoDirPath,
            String dstRepoId,
            String dstRepoDirPath,
            ArrayList<FolderDocumentEntity> selectedFolderDocumentEntities) {
        FolderTargetListDialogFragment fragment = new FolderTargetListDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_FOLDER_ACTION_TYPE, folderActionType);
        args.putString(KEY_SEA_FILE_FROM_REPO_ID, fromRepoId);
        args.putString(KEY_SEA_FILE_FROM_DIR_PATH, fromRepoDirPath);

        args.putString(KEY_SEA_FILE_DST_REPO_ID, dstRepoId);
        args.putString(KEY_SEA_FILE_DST_DIR_PATH, dstRepoDirPath);

        args.putSerializable(KEY_SEA_FILE_SELCTED_FILES, selectedFolderDocumentEntities);
        fragment.setArguments(args);
        return fragment;
    }

    @Const.FILE_ACTION_TYPE
    private int getFileActionType() {
        switch (getArguments().getInt(KEY_FOLDER_ACTION_TYPE)) {
            case FILE_ACTION_COPY:
                return FILE_ACTION_COPY;
            case FILE_ACTION_MOVE:
                return FILE_ACTION_MOVE;
        }
        return FILE_ACTION_MOVE;
    }

    /**
     * 源仓库id
     *
     * @return
     */
    protected String getSeaFileFromRepoId() {
        return getArguments().getString(KEY_SEA_FILE_FROM_REPO_ID, "");
    }

    /**
     * 源仓库地址
     *
     * @return
     */
    protected String getSeaFileFromDirPath() {
        return getArguments().getString(KEY_SEA_FILE_FROM_DIR_PATH, "");
    }

    /**
     * 目标仓库id
     *
     * @return
     */
    protected String getSeaFileDstRepoId() {
        return getArguments().getString(KEY_SEA_FILE_DST_REPO_ID, "");
    }

    /**
     * 目标仓库路径
     *
     * @return
     */
    protected String getSeaFileDstDirPath() {
        return getArguments().getString(KEY_SEA_FILE_DST_DIR_PATH, "");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(R.layout.dialog_fragment_folder_targt_list, inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog()
                    .getWindow()
                    .setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    protected void initView() {
        switch (getFileActionType()) {
            case FILE_ACTION_COPY:
                titleContent.setText("复制到");
                break;
            case FILE_ACTION_MOVE:
                titleContent.setText("移动到");
                break;
            default:
                titleContent.setText("复制到");
                break;
        }
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams attributes = window.getAttributes();
                attributes.windowAnimations = R.style.SlideAnimBottom;
                window.setAttributes(attributes);
            }
        }
        replaceFolderFragmemt(
                getSeaFileDstRepoId(),
                getSeaFileDstDirPath());
        getData(true);
    }

    @Override
    protected void getData(boolean isRefresh) {
        super.getData(isRefresh);
        callEnqueue(getSFileApi().repoDetailsQuery(getSeaFileFromRepoId()),
                new SFileCallBack<RepoEntity>() {
                    @Override
                    public void onSuccess(Call<RepoEntity> call, Response<RepoEntity> response) {
                        repoEntity = response.body();
                        if (!canBack2ParentDir() && foldrParentTv != null) {
                            foldrParentTv.setText(repoEntity != null ? repoEntity.repo_name : "");
                        }
                    }
                });
    }

    @OnClick({R.id.titleBack,
            R.id.foldr_go_parent_tv})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.titleBack:
                dismiss();
                break;
            case R.id.foldr_go_parent_tv:
                if (canBack2ParentDir()) {
                    back2ParentDir();
                } else {
                    replaceRepoFragmemt();
                }
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    /**
     * 是否可以返回到sfile父目录
     *
     * @return
     */
    private boolean canBack2ParentDir() {
        String dirPath = getSeaFileDstDirPath();
        if (TextUtils.equals(dirPath, "/")) {
            return false;
        }
        return true;
    }

    /**
     * 获取sfile父目录名字
     *
     * @return
     */
    private String getDstParentDirName() {
        String dirPath = getSeaFileDstDirPath();
        String spilteStr = "/";
        String[] split = dirPath.split(spilteStr);
        if (split != null && split.length > 0) {
            return split[split.length - 1];
        }
        return null;
    }

    /**
     * 返回到sfile父目录
     */
    private void back2ParentDir() {
        String dirPath = getSeaFileDstDirPath();
        String spilteStr = "/";
        if (dirPath.endsWith(spilteStr)) {
            String[] split = dirPath.split(spilteStr);
            if (split != null && split.length > 1) {
                String lastDir = split[split.length - 1];
                String parentDir = dirPath.substring(0, dirPath.length() - (lastDir.length() + spilteStr.length()));
                replaceFolderFragmemt(
                        getSeaFileDstRepoId(),
                        parentDir);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * @param fragment
     * @param type     1 替换 -1,消失
     * @param params
     */
    @Override
    public void onFragmentCallBack(Fragment fragment, int type, Bundle params) {
        if (fragment instanceof RepoSelectListFragment && params != null) {
            Serializable serializable = params.getSerializable(KEY_FRAGMENT_RESULT);
            if (serializable instanceof RepoEntity) {
                this.repoEntity = (RepoEntity) serializable;
                replaceFolderFragmemt(
                        repoEntity.repo_id,
                        "/");
            }
        } else {
            if (type == 1 && params != null) {
                replaceFolderFragmemt(
                        params.getString(KEY_SEA_FILE_DST_REPO_ID),
                        params.getString(KEY_SEA_FILE_DST_DIR_PATH));
            } else if (type == -1) {
                dismiss();
            }
        }
    }

    /**
     * 替换资料库选择页面
     */
    private void replaceRepoFragmemt() {
        currFragment = addOrShowFragment(
                RepoSelectListFragment.newInstance(),
                currFragment,
                R.id.main_fl_content);
        dirPathTitleLayout.setVisibility(View.GONE);
    }

    private void replaceFolderFragmemt(String seaFileRepoId,
                                       String seaFileParentDirPath) {
        dirPathTitleLayout.setVisibility(View.VISIBLE);
        getArguments().putString(KEY_SEA_FILE_DST_REPO_ID, seaFileRepoId);
        getArguments().putString(KEY_SEA_FILE_DST_DIR_PATH, seaFileParentDirPath);
        currFragment = addOrShowFragment(
                FolderTargetListFragment.newInstance(
                        getFileActionType(),
                        getSeaFileFromRepoId(),
                        getSeaFileFromDirPath(),
                        getSeaFileDstRepoId(),
                        getSeaFileDstDirPath(),
                        (ArrayList<FolderDocumentEntity>) getArguments().getSerializable(KEY_SEA_FILE_SELCTED_FILES)),
                currFragment,
                R.id.main_fl_content);
        if (canBack2ParentDir()) {
            foldrParentTv.setText(getDstParentDirName());
        } else {
            foldrParentTv.setText(repoEntity != null ? repoEntity.repo_name : "");
        }
    }
}
