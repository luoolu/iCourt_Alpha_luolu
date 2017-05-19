package com.icourt.alpha.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.icourt.alpha.R;
import com.icourt.alpha.base.BaseActivity;
import com.icourt.alpha.entity.bean.ContactDeatilBean;
import com.icourt.alpha.entity.event.UpdateCustomerEvent;
import com.icourt.alpha.http.callback.SimpleCallBack;
import com.icourt.alpha.http.httpmodel.ResEntity;
import com.icourt.alpha.utils.DateUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Description  个人联系人 详情
 * Company Beijing icourt
 * author  lu.zhao  E-mail:zhaolu@icourt.cc
 * date createTime：17/5/18
 * version 2.0.0
 */

public class CustomerPersonDetailActivity extends BaseActivity {

    String contact_id, contact_name;
    @BindView(R.id.titleBack)
    ImageView titleBack;
    @BindView(R.id.titleContent)
    TextView titleContent;
    @BindView(R.id.titleAction)
    ImageView titleAction;
    @BindView(R.id.titleAction2)
    ImageView titleAction2;
    @BindView(R.id.titleView)
    AppBarLayout titleView;
    @BindView(R.id.activity_person_contact_detail_name_view)
    TextView activityPersonContactDetailNameView;
    @BindView(R.id.activity_person_contact_detail_photo_view)
    ImageView activityPersonContactDetailPhotoView;
    @BindView(R.id.activity_person_contact_detail_group_layout)
    LinearLayout activityPersonContactDetailGroupLayout;
    @BindView(R.id.activity_person_contact_detail_other_parent_layout)
    LinearLayout activityPersonContactDetailOtherParentLayout;
    @BindView(R.id.activity_person_contact_detail_liaisons_parent_layout)
    LinearLayout activityPersonContactDetailLiaisonsParentLayout;
    @BindView(R.id.activity_person_contact_detail_impress_textview)
    TextView activityPersonContactDetailImpressTextview;
    @BindView(R.id.activity_person_contact_detail_impress_parent_layout)
    LinearLayout activityPersonContactDetailImpressParentLayout;
    @BindView(R.id.activity_person_contact_detail_creat_name_view)
    TextView activityPersonContactDetailCreatNameView;
    @BindView(R.id.activity_person_contact_detail_creat_date_view)
    TextView activityPersonContactDetailCreatDateView;
    @BindView(R.id.activity_person_contact_detail_creat_parent_layout)
    LinearLayout activityPersonContactDetailCreatParentLayout;

    private LayoutInflater layoutInflater;
    private boolean isShowRightView;
    private ContactDeatilBean contactDeatilBean;
    private List<ContactDeatilBean> liaisonsList;

    public static void launch(@NonNull Context context, @NonNull String contact_id, @NonNull String contact_name, @NonNull boolean isShowRightView) {
        if (context == null) return;
        Intent intent = new Intent(context, CustomerPersonDetailActivity.class);
        intent.putExtra("contact_id", contact_id);
        intent.putExtra("contact_name", contact_name);
        intent.putExtra("isShowRightView", isShowRightView);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_contact_detail_layout);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    protected void initView() {
        super.initView();
        layoutInflater = LayoutInflater.from(this);
        EventBus.getDefault().register(this);
        titleAction.setImageResource(R.mipmap.header_icon_star_line);
        titleAction2.setImageResource(R.mipmap.header_icon_edit);
        contact_id = getIntent().getStringExtra("contact_id");
        contact_name = getIntent().getStringExtra("contact_name");
        isShowRightView = getIntent().getBooleanExtra("isShowRightView", false);
        titleAction.setVisibility(isShowRightView ? View.VISIBLE : View.GONE);
        titleAction2.setVisibility(isShowRightView ? View.VISIBLE : View.GONE);
        if (!TextUtils.isEmpty(contact_name)) {
            setTitle(contact_name);
        }
        getContact();
    }

    @OnClick({R.id.titleAction,R.id.titleAction2})
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.titleAction:
                if (contactDeatilBean != null) {
                    if (contactDeatilBean.getContact() != null) {
                        if (contactDeatilBean.getContact().getIsView() == 0) {
                            addStarContact(contactDeatilBean.getContact().getPkid());
                        } else if (contactDeatilBean.getContact().getIsView() == 1) {
                            deleteStarContact(contactDeatilBean.getContact().getPkid());
                        }
                    }
                }
                break;
            case R.id.titleAction2:
                CustomerPersonCreateActivity.launch(this,contactDeatilBean,CustomerPersonCreateActivity.UPDATE_CUSTOMER_ACTION);
                break;
        }
    }

    private void setDataToView() {
        if (contactDeatilBean != null) {
            if (contactDeatilBean.getContact() != null) {
                if (contactDeatilBean.getContact().getIsView() == 0) {
                    titleAction.setImageResource(R.mipmap.header_icon_star_line);
                } else if (contactDeatilBean.getContact().getIsView() == 1) {
                    titleAction.setImageResource(R.mipmap.header_icon_star_solid);
                } else {
                    titleAction.setImageResource(R.mipmap.header_icon_star_line);
                }
                getLiaisons(contactDeatilBean.getContact().getPkid());
                activityPersonContactDetailNameView.setText(contactDeatilBean.getContact().getName());
                if ("P".equals(contactDeatilBean.getContact().getContactType())) {
                    if ("女".equals(contactDeatilBean.getContact().getSex())) {
                        activityPersonContactDetailPhotoView.setImageResource(R.mipmap.female);
                    } else {
                        activityPersonContactDetailPhotoView.setImageResource(R.mipmap.male);
                    }
                }

                if (!TextUtils.isEmpty(contactDeatilBean.getContact().getImpression())) {
                    activityPersonContactDetailImpressParentLayout.setVisibility(View.VISIBLE);
                    activityPersonContactDetailImpressTextview.setText(contactDeatilBean.getContact().getImpression());
                } else {
                    activityPersonContactDetailImpressParentLayout.setVisibility(View.GONE);
                }

                if (!TextUtils.isEmpty(contactDeatilBean.getContact().getCrtUserName()) && contactDeatilBean.getContact().getCrtTime() > 0) {
                    activityPersonContactDetailCreatParentLayout.setVisibility(View.VISIBLE);
                    activityPersonContactDetailCreatNameView.setText(contactDeatilBean.getContact().getCrtUserName());
                    activityPersonContactDetailCreatDateView.setText(" 创建于 " + DateUtils.getTimeDateFormatYear(contactDeatilBean.getContact().getCrtTime()));
                } else {
                    activityPersonContactDetailCreatParentLayout.setVisibility(View.GONE);
                }
            }
            activityPersonContactDetailOtherParentLayout.removeAllViews();
            activityPersonContactDetailGroupLayout.removeAllViews();
            if (contactDeatilBean.getGroups() != null) {
                if (contactDeatilBean.getGroups().size() > 0)
                    addGroupItemView(contactDeatilBean.getGroups());
            }
            if (contactDeatilBean.getUsedNames() != null) {
                if (contactDeatilBean.getUsedNames().size() > 0)
                    addUsedNameView(contactDeatilBean.getUsedNames());
            }
            if (contactDeatilBean.getTels() != null) {
                if (contactDeatilBean.getTels().size() > 0)
                    addTelsView(contactDeatilBean.getTels());
            }
            if (contactDeatilBean.getMails() != null) {
                if (contactDeatilBean.getMails().size() > 0)
                    addMailsView(contactDeatilBean.getMails());
            }
            if (contactDeatilBean.getAddresses() != null) {
                if (contactDeatilBean.getAddresses().size() > 0)
                    addAddressView(contactDeatilBean.getAddresses());
            }
            if (contactDeatilBean.getCompanies() != null) {
                if (contactDeatilBean.getCompanies().size() > 0)
                    addCompaniesView(contactDeatilBean.getCompanies());
            }
            if (contactDeatilBean.getCertificates() != null) {
                if (contactDeatilBean.getCertificates().size() > 0)
                    addCertificateView(contactDeatilBean.getCertificates());
            }
            if (contactDeatilBean.getIms() != null) {
                if (contactDeatilBean.getIms().size() > 0)
                    addImsView(contactDeatilBean.getIms());
            }
            if (contactDeatilBean.getDates() != null) {
                if (contactDeatilBean.getDates().size() > 0)
                    addDateView(contactDeatilBean.getDates());
            }
            if (contactDeatilBean.getUsedNames() == null &&
                    contactDeatilBean.getTels() == null &&
                    contactDeatilBean.getMails() == null &&
                    contactDeatilBean.getAddresses() == null &&
                    contactDeatilBean.getCompanies() == null &&
                    contactDeatilBean.getCertificates() == null &&
                    contactDeatilBean.getIms() == null &&
                    contactDeatilBean.getDates() == null) {
                // detailOtherParentLayout.setVisibility(View.GONE);
            }
        }
    }

    private void setAutoLink(TextView textView) {
        textView.setAutoLinkMask(Linkify.ALL);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * 添加itemview子view
     *
     * @param object
     * @return
     */
    private View getItemSonView(Object object, boolean isShowLine) {
        View sonView = layoutInflater.inflate(R.layout.person_contact_detail_item_sonview_layout, null);
        TextView keyView = (TextView) sonView.findViewById(R.id.person_contact_detail_item_key_name_view);
        TextView valueView = (TextView) sonView.findViewById(R.id.person_contact_detail_item_key_value_view);
        View line = sonView.findViewById(R.id.person_contact_detail_item_line_view);
        if (object instanceof ContactDeatilBean.GroupsBean) {
            ContactDeatilBean.GroupsBean groupbean = (ContactDeatilBean.GroupsBean) object;
            valueView.setVisibility(View.GONE);
            keyView.setText(groupbean.getGroupName());
        } else if (object instanceof ContactDeatilBean.UsedNamesBean) {
            ContactDeatilBean.UsedNamesBean usedName = (ContactDeatilBean.UsedNamesBean) object;
            keyView.setText(usedName.getItemValue());
            valueView.setVisibility(View.GONE);
        } else if (object instanceof ContactDeatilBean.TelsBean) {
            ContactDeatilBean.TelsBean tel = (ContactDeatilBean.TelsBean) object;
            keyView.setText(tel.getItemValue());
            setAutoLink(keyView);
            if (!TextUtils.isEmpty(tel.getItemSubType())) {
                valueView.setVisibility(View.VISIBLE);
                valueView.setText(tel.getItemSubType());
            } else {
                valueView.setVisibility(View.GONE);
            }
        } else if (object instanceof ContactDeatilBean.MailsBean) {
            ContactDeatilBean.MailsBean mail = (ContactDeatilBean.MailsBean) object;
            keyView.setText(mail.getItemValue());
            setAutoLink(keyView);
            if (!TextUtils.isEmpty(mail.getItemSubType())) {
                valueView.setVisibility(View.VISIBLE);
                valueView.setText(mail.getItemSubType());
            } else {
                valueView.setVisibility(View.GONE);
            }
        } else if (object instanceof ContactDeatilBean.AddressesBean) {
            ContactDeatilBean.AddressesBean address = (ContactDeatilBean.AddressesBean) object;
            keyView.setText(address.getItemValue());
            if (!TextUtils.isEmpty(address.getItemSubType())) {
                valueView.setVisibility(View.VISIBLE);
                valueView.setText(address.getItemSubType());
            } else {
                valueView.setVisibility(View.GONE);
            }
        } else if (object instanceof ContactDeatilBean.CompaniesBean) {
            ContactDeatilBean.CompaniesBean company = (ContactDeatilBean.CompaniesBean) object;
            keyView.setText((String) company.getItemValue());
            if (!TextUtils.isEmpty((String) company.getItemSubType())) {
                valueView.setVisibility(View.VISIBLE);
                valueView.setText((String) company.getItemSubType());
            } else {
                valueView.setVisibility(View.GONE);
            }
        } else if (object instanceof ContactDeatilBean.CertificatesBean) {
            ContactDeatilBean.CertificatesBean certificate = (ContactDeatilBean.CertificatesBean) object;
            keyView.setText(certificate.getItemValue());
            if (!TextUtils.isEmpty(certificate.getItemSubType())) {
                valueView.setVisibility(View.VISIBLE);
                valueView.setText(certificate.getItemSubType());
            } else {
                valueView.setVisibility(View.GONE);
            }
        } else if (object instanceof ContactDeatilBean.ImsBean) {
            ContactDeatilBean.ImsBean im = (ContactDeatilBean.ImsBean) object;
            keyView.setText(im.getItemValue());
            if (!TextUtils.isEmpty(im.getItemSubType())) {
                valueView.setVisibility(View.VISIBLE);
                valueView.setText(im.getItemSubType());
            } else {
                valueView.setVisibility(View.GONE);
            }
        } else if (object instanceof ContactDeatilBean.DateBean) {
            ContactDeatilBean.DateBean datebean = (ContactDeatilBean.DateBean) object;
            keyView.setText(datebean.getItemValue());
            if (!TextUtils.isEmpty(datebean.getItemSubType())) {
                valueView.setVisibility(View.VISIBLE);
                valueView.setText(datebean.getItemSubType());
            } else {
                valueView.setVisibility(View.GONE);
            }
        }
        line.setVisibility(isShowLine ? View.VISIBLE : View.GONE);
        return sonView;
    }

    /**
     * 添加groupview
     *
     * @return
     */
    private void addGroupItemView(List<ContactDeatilBean.GroupsBean> groups) {
        View parentView = layoutInflater.inflate(R.layout.person_contact_detail_item_layout, null);
        TextView formView = (TextView) parentView.findViewById(R.id.person_contact_detail_item_form_name_view);
        LinearLayout formValueView = (LinearLayout) parentView.findViewById(R.id.person_contact_detail_item_form_value_view);
        formView.setText("负责团队");
        for (int i = 0; i < groups.size(); i++) {
            if (i == groups.size() - 1) {
                formValueView.addView(getItemSonView(groups.get(i), false));
            } else {
                formValueView.addView(getItemSonView(groups.get(i), true));
            }
        }
        activityPersonContactDetailGroupLayout.addView(parentView);
    }

    /**
     * 添加曾用名view
     */
    private void addUsedNameView(List<ContactDeatilBean.UsedNamesBean> usedNames) {
        View parentView = layoutInflater.inflate(R.layout.person_contact_detail_item_layout, null);
        TextView formView = (TextView) parentView.findViewById(R.id.person_contact_detail_item_form_name_view);
        LinearLayout formValueView = (LinearLayout) parentView.findViewById(R.id.person_contact_detail_item_form_value_view);
        formView.setText("曾用名");
        for (int i = 0; i < usedNames.size(); i++) {
            if (i == usedNames.size() - 1) {
                formValueView.addView(getItemSonView(usedNames.get(i), false));
            } else {
                formValueView.addView(getItemSonView(usedNames.get(i), true));
            }
        }
        activityPersonContactDetailOtherParentLayout.addView(parentView);
    }

    /**
     * 添加电话view
     */
    private void addTelsView(List<ContactDeatilBean.TelsBean> tels) {
        View parentView = layoutInflater.inflate(R.layout.person_contact_detail_item_layout, null);
        TextView formView = (TextView) parentView.findViewById(R.id.person_contact_detail_item_form_name_view);
        LinearLayout formValueView = (LinearLayout) parentView.findViewById(R.id.person_contact_detail_item_form_value_view);
        if (tels != null) {
            formView.setText("电话");
            for (int i = 0; i < tels.size(); i++) {
                if (i == tels.size() - 1) {
                    formValueView.addView(getItemSonView(tels.get(i), false), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                } else {
                    formValueView.addView(getItemSonView(tels.get(i), true), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
            }
            parentView.setVisibility(View.VISIBLE);
            activityPersonContactDetailOtherParentLayout.setVisibility(View.VISIBLE);
            activityPersonContactDetailOtherParentLayout.addView(parentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    /**
     * 添加邮箱View
     *
     * @param mails
     */
    private void addMailsView(List<ContactDeatilBean.MailsBean> mails) {
        View parentView = layoutInflater.inflate(R.layout.person_contact_detail_item_layout, null);
        TextView formView = (TextView) parentView.findViewById(R.id.person_contact_detail_item_form_name_view);
        LinearLayout formValueView = (LinearLayout) parentView.findViewById(R.id.person_contact_detail_item_form_value_view);
        if (mails != null) {
            formView.setText("邮箱");
            for (int i = 0; i < mails.size(); i++) {
                if (i == mails.size() - 1) {
                    formValueView.addView(getItemSonView(mails.get(i), false));
                } else {
                    formValueView.addView(getItemSonView(mails.get(i), true));
                }
            }
            activityPersonContactDetailOtherParentLayout.addView(parentView);
        }
    }

    /**
     * 添加地址View
     *
     * @param addreeses
     */
    private void addAddressView(List<ContactDeatilBean.AddressesBean> addreeses) {
        View parentView = layoutInflater.inflate(R.layout.person_contact_detail_item_layout, null);
        TextView formView = (TextView) parentView.findViewById(R.id.person_contact_detail_item_form_name_view);
        LinearLayout formValueView = (LinearLayout) parentView.findViewById(R.id.person_contact_detail_item_form_value_view);
        if (addreeses != null) {
            formView.setText("地址");
            for (int i = 0; i < addreeses.size(); i++) {
                if (i == addreeses.size() - 1) {
                    formValueView.addView(getItemSonView(addreeses.get(i), false));
                } else {
                    formValueView.addView(getItemSonView(addreeses.get(i), true));
                }
            }
            activityPersonContactDetailOtherParentLayout.addView(parentView);
        }
    }

    /**
     * 添加工作单位View
     *
     * @param companies
     */
    private void addCompaniesView(List<ContactDeatilBean.CompaniesBean> companies) {
        View parentView = layoutInflater.inflate(R.layout.person_contact_detail_item_layout, null);
        TextView formView = (TextView) parentView.findViewById(R.id.person_contact_detail_item_form_name_view);
        LinearLayout formValueView = (LinearLayout) parentView.findViewById(R.id.person_contact_detail_item_form_value_view);
        if (companies != null) {
            formView.setText("工作单位");
            for (int i = 0; i < companies.size(); i++) {
                if (i == companies.size() - 1) {
                    formValueView.addView(getItemSonView(companies.get(i), false));
                } else {
                    formValueView.addView(getItemSonView(companies.get(i), true));
                }
            }
            activityPersonContactDetailOtherParentLayout.addView(parentView);
        }
    }

    /**
     * 添加个人证件View
     *
     * @param certificates
     */
    private void addCertificateView(List<ContactDeatilBean.CertificatesBean> certificates) {
        View parentView = layoutInflater.inflate(R.layout.person_contact_detail_item_layout, null);
        TextView formView = (TextView) parentView.findViewById(R.id.person_contact_detail_item_form_name_view);
        LinearLayout formValueView = (LinearLayout) parentView.findViewById(R.id.person_contact_detail_item_form_value_view);
        if (certificates != null) {
            formView.setText("个人证件");
            for (int i = 0; i < certificates.size(); i++) {
                if (i == certificates.size() - 1) {
                    formValueView.addView(getItemSonView(certificates.get(i), false));
                } else {
                    formValueView.addView(getItemSonView(certificates.get(i), true));
                }
            }
            activityPersonContactDetailOtherParentLayout.addView(parentView);
        }
    }

    /**
     * 添加即时通讯号码View
     *
     * @param ims
     */
    private void addImsView(List<ContactDeatilBean.ImsBean> ims) {
        View parentView = layoutInflater.inflate(R.layout.person_contact_detail_item_layout, null);
        TextView formView = (TextView) parentView.findViewById(R.id.person_contact_detail_item_form_name_view);
        LinearLayout formValueView = (LinearLayout) parentView.findViewById(R.id.person_contact_detail_item_form_value_view);
        if (ims != null) {
            formView.setText("即时通讯号码");
            for (int i = 0; i < ims.size(); i++) {
                if (i == ims.size() - 1) {
                    formValueView.addView(getItemSonView(ims.get(i), false));
                } else {
                    formValueView.addView(getItemSonView(ims.get(i), true));
                }
            }
            activityPersonContactDetailOtherParentLayout.addView(parentView);
        }
    }

    /**
     * 添加重要日期View
     *
     * @param dates
     */
    private void addDateView(List<ContactDeatilBean.DateBean> dates) {
        View parentView = layoutInflater.inflate(R.layout.person_contact_detail_item_layout, null);
        TextView formView = (TextView) parentView.findViewById(R.id.person_contact_detail_item_form_name_view);
        LinearLayout formValueView = (LinearLayout) parentView.findViewById(R.id.person_contact_detail_item_form_value_view);
        if (dates != null) {
            formView.setText("重要日期");
            for (int i = 0; i < dates.size(); i++) {
                if (i == dates.size() - 1) {
                    formValueView.addView(getItemSonView(dates.get(i), false));
                } else {
                    formValueView.addView(getItemSonView(dates.get(i), true));
                }
            }
            activityPersonContactDetailOtherParentLayout.addView(parentView);
        }
    }

    private void setLiaisonsView() {
        if (liaisonsList != null) {
            if (liaisonsList.size() > 0) {
                activityPersonContactDetailLiaisonsParentLayout.setVisibility(View.VISIBLE);
                List<ContactDeatilBean> personList = new ArrayList<>();//个人联络人
                List<ContactDeatilBean> companyList = new ArrayList<>();//企业联络人
                for (ContactDeatilBean deatilBean : liaisonsList) {
                    if ("P".equals(deatilBean.getContact().getContactType())) {
                        personList.add(deatilBean);
                    } else if ("C".equals(deatilBean.getContact().getContactType())) {
                        companyList.add(deatilBean);
                    }
                }
                activityPersonContactDetailLiaisonsParentLayout.removeAllViews();
                addLiaisonView("P", personList);
                addLiaisonView("C", companyList);
            } else {
                activityPersonContactDetailLiaisonsParentLayout.setVisibility(View.GONE);
            }
        } else {
            activityPersonContactDetailLiaisonsParentLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 添加联络人view
     */
    private void addLiaisonView(final String type, final List<ContactDeatilBean> liaisonsList) {
        View parentView = layoutInflater.inflate(R.layout.person_contact_detail_item_layout, null);
        final TextView formView = (TextView) parentView.findViewById(R.id.person_contact_detail_item_form_name_view);
        LinearLayout formValueView = (LinearLayout) parentView.findViewById(R.id.person_contact_detail_item_form_value_view);


        if (liaisonsList != null) {
            if (liaisonsList.size() > 0) {
                if ("P".equals(type)) {
                    formView.setText("关联人");
                } else if ("C".equals(type)) {
                    formView.setText("关联机构");
                }
                for (int i = 0; i < liaisonsList.size(); i++) {
                    final ContactDeatilBean c = liaisonsList.get(i);
                    View sonView = layoutInflater.inflate(R.layout.person_contact_detail_item_sonview_layout, null);
                    TextView keyView = (TextView) sonView.findViewById(R.id.person_contact_detail_item_key_name_view);
                    TextView valueView = (TextView) sonView.findViewById(R.id.person_contact_detail_item_key_value_view);
                    View line = sonView.findViewById(R.id.person_contact_detail_item_line_view);
                    if (i == liaisonsList.size() - 1) {
                        line.setVisibility(View.GONE);
                    } else {
                        line.setVisibility(View.VISIBLE);
                    }
                    keyView.setText(c.getContact().getName());
                    if (c.getCompanies() != null) {
                        if (c.getCompanies().size() > 0) {
                            if (!TextUtils.isEmpty(String.valueOf(c.getCompanies().get(0).getItemSubType())) && !TextUtils.equals("null", String.valueOf(c.getCompanies().get(0).getItemSubType()))) {
                                valueView.setVisibility(View.VISIBLE);
                                valueView.setText(String.valueOf(c.getCompanies().get(0).getItemSubType()));
                            } else {
                                valueView.setVisibility(View.GONE);
                            }
                        } else {
                            valueView.setVisibility(View.GONE);
                        }
                    } else {
                        valueView.setVisibility(View.GONE);
                    }
                    sonView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (TextUtils.equals("P",type)) {
                                CustomerPersonDetailActivity.launch(getContext(), c.getContact().getPkid(), c.getContact().getName(), false);
                            } else if (TextUtils.equals("C",type)) {
                                CustomerCompanyDetailActivity.launch(getContext(), c.getContact().getPkid(), c.getContact().getName(), false);
                            }

                        }
                    });
                    formValueView.addView(sonView);
                }
                activityPersonContactDetailLiaisonsParentLayout.addView(parentView);
            }
        }

    }

    /**
     * 获取联系人详情
     */
    private void getContact() {
        showLoadingDialog(null);
        getApi().customerDetailQuery(contact_id).enqueue(new SimpleCallBack<List<ContactDeatilBean>>() {
            @Override
            public void onSuccess(Call<ResEntity<List<ContactDeatilBean>>> call, Response<ResEntity<List<ContactDeatilBean>>> response) {
                if (response.body().result != null) {
                    if (response.body().result.size() > 0) {
                        dismissLoadingDialog();
                        contactDeatilBean = response.body().result.get(0);
                        setDataToView();
                    }
                }

            }

            @Override
            public void onFailure(Call<ResEntity<List<ContactDeatilBean>>> call, Throwable t) {
                super.onFailure(call, t);
                dismissLoadingDialog();
            }
        });
    }

    /**
     * 获取企业联络人
     */
    private void getLiaisons(String id) {
        getApi().customerLiaisonsQuery(id).enqueue(new SimpleCallBack<List<ContactDeatilBean>>() {
            @Override
            public void onSuccess(Call<ResEntity<List<ContactDeatilBean>>> call, Response<ResEntity<List<ContactDeatilBean>>> response) {
                liaisonsList = response.body().result;
                setLiaisonsView();
            }
        });
    }

    /**
     * 添加关注
     *
     * @param contact_id
     */
    private void addStarContact(String contact_id) {
        showLoadingDialog("正在关注...");
        getApi().customerAddStar(contact_id).enqueue(new SimpleCallBack<JsonElement>() {
            @Override
            public void onSuccess(Call<ResEntity<JsonElement>> call, Response<ResEntity<JsonElement>> response) {
                dismissLoadingDialog();
                titleAction.setImageResource(R.mipmap.header_icon_star_solid);
                contactDeatilBean.getContact().setIsView(1);
            }

            @Override
            public void onFailure(Call<ResEntity<JsonElement>> call, Throwable t) {
                super.onFailure(call, t);
                dismissLoadingDialog();
                showTopSnackBar("关注失败");
            }
        });
    }

    /**
     * 删除关注
     *
     * @param contact_id
     */
    private void deleteStarContact(String contact_id) {
        showLoadingDialog("正在取消关注...");
        getApi().customerDeleteStar(contact_id).enqueue(new SimpleCallBack<JsonElement>() {
            @Override
            public void onSuccess(Call<ResEntity<JsonElement>> call, Response<ResEntity<JsonElement>> response) {
                dismissLoadingDialog();
                titleAction.setImageResource(R.mipmap.header_icon_star_line);
                contactDeatilBean.getContact().setIsView(0);
            }

            @Override
            public void onFailure(Call<ResEntity<JsonElement>> call, Throwable t) {
                super.onFailure(call, t);
                dismissLoadingDialog();
                showTopSnackBar("取消关注失败");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateCustEvent(UpdateCustomerEvent event) {
        if (event != null) {
            getContact();
        }
    }
}
