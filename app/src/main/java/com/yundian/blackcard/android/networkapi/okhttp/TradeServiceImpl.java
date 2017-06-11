package com.yundian.blackcard.android.networkapi.okhttp;

import com.yundian.blackcard.android.model.AccountInfo;
import com.yundian.blackcard.android.model.BalanceModel;
import com.yundian.blackcard.android.model.ButlerserviceInfo;
import com.yundian.blackcard.android.model.DeviceInfo;
import com.yundian.blackcard.android.model.MyPurseDetailModel;
import com.yundian.blackcard.android.model.PayInfo;
import com.yundian.blackcard.android.model.PurchaseHistoryModel;
import com.yundian.blackcard.android.model.UserInfo;
import com.yundian.blackcard.android.networkapi.ITradeService;
import com.yundian.blackcard.android.networkapi.IUserService;
import com.yundian.comm.networkapi.listener.OnAPIListener;
import com.yundian.comm.networkapi.obsserver.DefObserver;
import com.yundian.comm.networkapi.okhttp.OkHttpService;
import com.yundian.comm.networkapi.response.DefResponse;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by yaowang on 2017/5/11.
 */

public class TradeServiceImpl extends OkHttpService<TradeServiceImpl.RetrofitUserService> implements ITradeService {


    public interface RetrofitUserService {

        @FormUrlEncoded
        @POST("/api/trade/usertrades.json")
        Observable<DefResponse<List<PurchaseHistoryModel>>> userTrades(@Field("page") int page);

        @FormUrlEncoded
        @POST("/api/butlerservice/info.json")
        Observable<DefResponse<ButlerserviceInfo>> butlerserviceInfo(@Field("serviceNo") String serviceNo);

         @FormUrlEncoded
        @POST("/api/butlerservice/pay.json")
        Observable<DefResponse<PayInfo>> butlerservicePay(@Field("serviceNo") String serviceNo,@Field("payType") int payType,@Field("payPassword") String payPassword);
    }

    @Override
    public void userTrades(int page, OnAPIListener<List<PurchaseHistoryModel>> listener) {
        setSubscribe(service.userTrades(page), new DefObserver<List<PurchaseHistoryModel>>(listener));
    }

    @Override
    public void butlerserviceInfo(String serviceNo, OnAPIListener<ButlerserviceInfo> listener) {
        setSubscribe(service.butlerserviceInfo(serviceNo), new DefObserver<ButlerserviceInfo>(listener));

    }

    @Override
    public void butlerservicePay(String serviceNo, int payType, String payPassword, OnAPIListener<PayInfo> listener) {
        setSubscribe(service.butlerservicePay(serviceNo, payType, payPassword), new DefObserver<PayInfo>(listener));

    }

}
