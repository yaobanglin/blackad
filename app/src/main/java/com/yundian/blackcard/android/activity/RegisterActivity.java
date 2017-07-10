package com.yundian.blackcard.android.activity;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.classic.adapter.BaseAdapterHelper;
import com.classic.adapter.CommonAdapter;
import com.yundian.blackcard.android.R;
import com.yundian.blackcard.android.adapter.GridViewPageAdapter;
import com.yundian.blackcard.android.adapter.GridViewPrivilegeInfoAdapter;
import com.yundian.blackcard.android.model.BlackcardInfo;
import com.yundian.blackcard.android.model.BlackcardInfos;
import com.yundian.blackcard.android.model.PrivilegeInfo;
import com.yundian.blackcard.android.model.RegisterInfo;
import com.yundian.blackcard.android.networkapi.NetworkAPIFactory;
import com.yundian.blackcard.android.util.AppUpdaterUtil;
import com.yundian.comm.networkapi.listener.OnAPIListener;
import com.yundian.comm.util.StringUtils;
import com.yundian.comm.util.ValidateUtils;
import com.yundian.viewpagerindicator.PageIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class RegisterActivity extends BaseActivity implements OnAPIListener<BlackcardInfos> {


    @BindView(R.id.viewpager)
    protected ViewPager viewPager;

    @BindView(R.id.pageindicator)
    protected PageIndicator pageIndicator;
    @BindView(R.id.blackcard_gridview)
    protected GridView blackcardGridview;
    @BindView(R.id.no_custom_name)
    LinearLayout noCustomName;
    @BindView(R.id.yes_custom_name)
    LinearLayout yesCustomName;
    @BindView(R.id.custom_name)
    EditText customName;
    @BindView(R.id.custom_name_tips)
    TextView customNameTips;

    @BindView(R.id.content)
    ScrollView scrollView;


    private BlackcardInfos blackcardInfos;
    private RegisterInfo registerInfo = new RegisterInfo();


    private static final int ITEM_COUNT_OF_PAGE = 8;


    @Override
    public void initData() {
        super.initData();
        setTitle(R.string.title_activity_register);
        AppUpdaterUtil.checkAppVersion(this);
        NetworkAPIFactory.getBlackcardService().blackcardInfos(this);
        showLoader("加载中...");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AppUpdaterUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }


    @Override
    public void onSuccess(BlackcardInfos blackcardInfos) {
        scrollView.setVisibility(View.VISIBLE);
        closeLoader();
        this.blackcardInfos = blackcardInfos;
        registerInfo.setCustomNamePrice(blackcardInfos.getCustomNamePrice());
        customNameTips.setText(String.format("定制姓名(¥%.2f)", blackcardInfos.getCustomNamePrice()));
        Integer index = blackcardInfos.getBlackcards().size() > 1 ? 1 : 0;
        registerInfo.setBlackcardInfo(blackcardInfos.getBlackcards().get(index));
        PrivilegeInfo.blackCardId = registerInfo.getBlackcardInfo().getBlackcardId().toString();
//        blackcardGridview.setNumColumns(blackcardInfos.getBlackcards().size());
        blackcardGridview.setAdapter(new CommonAdapter<BlackcardInfo>(this, R.layout.blackcard_item, blackcardInfos.getBlackcards()) {
            @Override
            public void onUpdate(BaseAdapterHelper helper, BlackcardInfo blackcardInfo, int position) {
                helper.setText(R.id.blackcard_name, blackcardInfo.getBlackcardName());
                helper.setText(R.id.blackcard_price, String.format("¥%.2f", blackcardInfo.getBlackcardPrice()));
                if (blackcardInfo.getBlackcardId().equals(registerInfo.getBlackcardInfo().getBlackcardId())) {
                    helper.setBackgroundRes(R.id.blackcard_layout, R.drawable.blackcard_item_sel_bg);
                } else {
                    helper.setBackgroundRes(R.id.blackcard_layout, R.drawable.edittext_bg);
                }
            }
        });
        bindPrivileges(0);
    }

    private void bindPrivileges(Integer index) {
        List<View> gridViews = new ArrayList<View>();
        List<List<PrivilegeInfo>> lists = blackcardInfos.privilegesSpit(ITEM_COUNT_OF_PAGE);
        for (List<PrivilegeInfo> privilegeInfos : lists) {
            GridView gridView = (GridView) LayoutInflater.from(this).inflate(R.layout.gridview_layout, null);
            BaseAdapter adapter = new GridViewPrivilegeInfoAdapter(this, R.layout.register_privilege_item, privilegeInfos);
            gridView.setAdapter(adapter);
            gridViews.add(gridView);
        }
        viewPager.setAdapter(new GridViewPageAdapter(gridViews));
        viewPager.setCurrentItem(index);
        pageIndicator.setViewPager(viewPager, index);
    }


    @Override
    public void initView() {
        super.initView();
        yesCustomName.setSelected(true);
        mToolbarRightImage.setVisibility(View.VISIBLE);
        mToolbarRightImage.setBackgroundResource(R.mipmap.call_butler);
    }


    @Override
    protected boolean isShowBackButton() {
        return true;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_register;
    }


    @OnClick({R.id.but_next, R.id.yes_custom_name, R.id.no_custom_name})
    public void onClick(View view) {
        if (view.getId() == R.id.but_next) {
            if (yesCustomName.isSelected()) {
                String strCustomName = customName.getText().toString().trim().toUpperCase();
                if (StringUtils.isEmpty(strCustomName)) {
                    showToast(customName.getHint());
                    return;
                } else if (!ValidateUtils.isEnName(strCustomName)) {
                    showToast(customName.getHint());
                    return;
                } else if (registerInfo.getBlackcardInfo().getBlackcardPrice() <= 0) {
                    showToast("当前会籍不能定制姓名");
                    return;
                }
                registerInfo.setCustomName(strCustomName);
            }
            Bundle bundle = new Bundle();
            bundle.putSerializable(RegisterInfo.class.getName(), registerInfo);
            Intent intent = new Intent();
            intent.putExtras(bundle);
            intent.setClass(this, RegisterInfoActivity.class);
            startActivityForResult(intent, 0);
        } else {
            if (view.getId() == R.id.yes_custom_name && !isPay()) {
                showToast(registerInfo.getBlackcardInfo().getBlackcardName() + "暂不支持定制姓名");
                customName.setEnabled(false);
                return;
            }
            yesCustomName.setSelected(false);
            noCustomName.setSelected(false);
            yesCustomName.setBackgroundResource(R.drawable.edittext_bg);
            noCustomName.setBackgroundResource(R.drawable.edittext_bg);
            view.setBackgroundResource(R.drawable.blackcard_item_sel_bg);
            view.setSelected(true);
            customName.setEnabled(view.equals(yesCustomName));
        }


    }

    @Override
    public void initListener() {
        super.initListener();
        blackcardGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BlackcardInfo blackcardInfo = (BlackcardInfo) parent.getAdapter().getItem(position);
                PrivilegeInfo.blackCardId = blackcardInfo.getBlackcardId().toString();
                bindPrivileges(viewPager.getCurrentItem());
                registerInfo.setBlackcardInfo(blackcardInfo);
                ((BaseAdapter) blackcardGridview.getAdapter()).notifyDataSetChanged();
                switchCustomName();
            }
        });

        mToolbarRightImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                String serviceUrl = "https://heika.qiyukf.com/client?k=9bcdb2182bde2835e2b0e7fc72a39e68&wp=1";
                if (blackcardInfos != null && StringUtils.isNotEmpty(blackcardInfos.getServiceUrl())) {
                    serviceUrl = blackcardInfos.getServiceUrl();
                }
                intent.setData(Uri.parse(serviceUrl));
                intent.setClass(RegisterActivity.this, WebViewActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean isPay() {
        return registerInfo.getBlackcardInfo().getBlackcardPrice() > 0;
    }

    private void switchCustomName() {
        if (isPay()) {
            yesCustomName.setSelected(true);
            noCustomName.setSelected(false);
            noCustomName.setBackgroundResource(R.drawable.edittext_bg);
            yesCustomName.setBackgroundResource(R.drawable.blackcard_item_sel_bg);
        } else {
            noCustomName.setSelected(true);
            yesCustomName.setSelected(false);
            yesCustomName.setBackgroundResource(R.drawable.edittext_bg);
            noCustomName.setBackgroundResource(R.drawable.blackcard_item_sel_bg);
        }
        customName.setEnabled(isPay());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            finish();
        }
    }
}
