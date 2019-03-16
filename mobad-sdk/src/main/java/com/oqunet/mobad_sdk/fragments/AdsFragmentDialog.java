package com.oqunet.mobad_sdk.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.oqunet.mobad_sdk.MobAd;
import com.oqunet.mobad_sdk.R;
import com.oqunet.mobad_sdk.adapters.CarouselAdItemsListAdapter;
import com.oqunet.mobad_sdk.database.AppDatabase;
import com.oqunet.mobad_sdk.database.entity.Ad;
import com.oqunet.mobad_sdk.database.entity.CarouselAdItem;
import com.oqunet.mobad_sdk.retrofit.ApiClient;
import com.oqunet.mobad_sdk.retrofit.ApiService;
import com.oqunet.mobad_sdk.retrofit.HandelErrors;
import com.oqunet.mobad_sdk.retrofit.entity.Action;
import com.oqunet.mobad_sdk.utils.Constants;
import com.oqunet.mobad_sdk.utils.ImageUtil;
import com.oqunet.mobad_sdk.utils.MobAdUtils;
import com.oqunet.mobad_sdk.utils.ViewAnimation;

import java.util.List;

import hb.xvideoplayer.MxVideoPlayer;
import hb.xvideoplayer.MxVideoPlayerWidget;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AdsFragmentDialog extends DialogFragment {
    private static final String LOG_TAG = AdsFragmentDialog.class.getSimpleName();
    public static final String ARG_ITEM_ID = "image_ad";
    ImageView advertiserBrandIcon, adImage;
    TextView advertiserName, adTitle, adDescription, earnedCoinsMessage;
    TextView ctaButton;
    ImageButton closeButton;
    CardView earnedCoinsLayout;
    Ad ad;
    ShowingAdInterface showingAdInterface;
    ApiService apiService;
    Call<Action> adActionCall;
    HandelErrors handelErrors;
    private Runnable runnable = null;
    private Handler handler = new Handler();
    MobAd mobAd;
    MxVideoPlayerWidget videoView;


    public AdsFragmentDialog() {
        // Required empty public constructor
    }

    public interface ShowingAdInterface {
        void onShownAd();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        if (context instanceof ShowingAdInterface) {
            showingAdInterface = (ShowingAdInterface) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement ShowingAdInterface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        assert bundle != null;
        ad = bundle.getParcelable("ad");

        mobAd = new MobAd(getActivity());
        mobAd.registerPhoneCallsReceiver();
    }


    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Dialog dialog = new Dialog(getActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    //    dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // set the layout for the ad dialog
        if (ad.getFormat().equals("Image")) {
            dialog.setContentView(R.layout.ad_image_layout);
            initializeImageAdViews(dialog);
            setImageAdDataAndListeners();
        } else if (ad.getFormat().equals("Video")) {
            dialog.setContentView(R.layout.ad_video_layout);
            initializeVideoAdViews(dialog);
            setVideoAdDataAndListeners();
        } else if (ad.getFormat().equals("Text")) {
            dialog.setContentView(R.layout.ad_text_layout);
            initializeTextAdViews(dialog);
            setTextAdDataAndListeners();
        } else if (ad.getFormat().equals("Carousel")) {
            dialog.setContentView(R.layout.ad_carousel_layout);
            initializeCarouselAdViews(dialog);
        }

        //    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.dialog_background_color));

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(layoutParams);

        sendAdAction(Constants.KEY_VIEWED);

        return dialog;
    }



    private void initializeImageAdViews(Dialog dialog) {
        advertiserBrandIcon = dialog.findViewById(R.id.advertiser_icon);
        adImage = dialog.findViewById(R.id.ad_image);
        advertiserName = dialog.findViewById(R.id.advertiser_name);
        adTitle = dialog.findViewById(R.id.title);
        adDescription = dialog.findViewById(R.id.description);
        ctaButton = dialog.findViewById(R.id.btn_cta);
        closeButton = dialog.findViewById(R.id.bt_close);
        earnedCoinsLayout = dialog.findViewById(R.id.earned_coins_layout);
        earnedCoinsMessage = dialog.findViewById(R.id.message);
        earnedCoinsLayout.setVisibility(View.INVISIBLE);
    }

    private void initializeVideoAdViews(Dialog dialog) {
        advertiserBrandIcon = dialog.findViewById(R.id.advertiser_icon);
        advertiserName = dialog.findViewById(R.id.advertiser_name);
        adTitle = dialog.findViewById(R.id.title);
        adDescription = dialog.findViewById(R.id.description);
        videoView = dialog.findViewById(R.id.ad_video);
        ctaButton = dialog.findViewById(R.id.btn_cta);
        closeButton = dialog.findViewById(R.id.bt_close);
        earnedCoinsLayout = dialog.findViewById(R.id.earned_coins_layout);
        earnedCoinsMessage = dialog.findViewById(R.id.message);
        earnedCoinsLayout.setVisibility(View.INVISIBLE);
    }

    private void initializeTextAdViews(Dialog dialog) {
        advertiserBrandIcon = dialog.findViewById(R.id.advertiser_icon);
        advertiserName = dialog.findViewById(R.id.advertiser_name);
        adTitle = dialog.findViewById(R.id.title);
        adDescription = dialog.findViewById(R.id.description);
        ctaButton = dialog.findViewById(R.id.btn_cta);
        closeButton = dialog.findViewById(R.id.bt_close);
        earnedCoinsLayout = dialog.findViewById(R.id.earned_coins_layout);
        earnedCoinsMessage = dialog.findViewById(R.id.message);
        earnedCoinsLayout.setVisibility(View.INVISIBLE);
    }

    private void initializeCarouselAdViews(Dialog dialog) {
        advertiserBrandIcon = dialog.findViewById(R.id.advertiser_icon);
        advertiserName = dialog.findViewById(R.id.advertiser_name);
        adTitle = dialog.findViewById(R.id.text);
        ctaButton = dialog.findViewById(R.id.btn_cta);
        closeButton = dialog.findViewById(R.id.bt_close);
        earnedCoinsLayout = dialog.findViewById(R.id.earned_coins_layout);
        earnedCoinsMessage = dialog.findViewById(R.id.message);
        earnedCoinsLayout.setVisibility(View.INVISIBLE);

        List<CarouselAdItem> carouselAdItems = AppDatabase.getInstance(getActivity()).getCarouselAdItemDao().loadCarouselItems();

        RecyclerView carouselAdRecyclerView = dialog.findViewById(R.id.carousel_ad_recyclerView);
        LinearLayoutManager layoutManagerForCarouselAdRecyclerView = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        carouselAdRecyclerView.setLayoutManager(layoutManagerForCarouselAdRecyclerView);
        carouselAdRecyclerView.setVisibility(View.VISIBLE);

        adTitle.setText(ad.getAdTitle());

        CarouselAdItemsListAdapter carouselAdItemsListAdapter = new CarouselAdItemsListAdapter(getActivity());
        carouselAdItemsListAdapter.setCarouselAdItemsList(carouselAdItems);
        carouselAdRecyclerView.setAdapter(carouselAdItemsListAdapter);
        carouselAdItemsListAdapter.setOnItemClickListener(new CarouselAdItemsListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, com.oqunet.mobad_sdk.database.entity.CarouselAdItem carouselAdItem, int position) {
                dismiss();
                showingAdInterface.onShownAd();
                MobAdUtils.startNewActivity(getActivity(), "com.oqunet.mobad");

            }

        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }



    private void setImageAdDataAndListeners() {
        if (ad != null) {
            ImageUtil.displayRoundImage(advertiserBrandIcon, "https://" + ad.getAdvertiserImage(), null);
            ImageUtil.displayImage(adImage, "https://" + ad.getAdPath(), null);
            advertiserName.setText(ad.getAdvertiserName());
            adTitle.setText(ad.getAdTitle());
            adDescription.setText(ad.getAdDescription());
            ctaButton.setText(ad.getButtonName());

            ctaButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendAdAction(Constants.KEY_CLICKED);
                    dismiss();
                    MobAdUtils.openWebUrlExternal(getActivity(), ad.getButtonLink());
                }
            });

            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
        }

    }

    private void setVideoAdDataAndListeners() {
        if (ad != null) {
            ImageUtil.displayRoundImage(advertiserBrandIcon, "https://" + ad.getAdvertiserImage(), null);
            videoView.autoStartPlay("https://admob.azurewebsites.net/content/ad_videos/" + ad.getAdPath(),
                    MxVideoPlayer.SCREEN_LAYOUT_NORMAL, ad.getAdTitle());
            advertiserName.setText(ad.getAdvertiserName());
            adTitle.setText(ad.getAdTitle());
            adDescription.setText(ad.getAdDescription());
            ctaButton.setText(ad.getButtonName());

            ctaButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendAdAction(Constants.KEY_CLICKED);
                    dismiss();
                    MobAdUtils.openWebUrlExternal(getActivity(), ad.getButtonLink());
                }
            });

            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();

                }
            });
        }

    }

    private void setTextAdDataAndListeners() {
        if (ad != null) {
            ImageUtil.displayRoundImage(advertiserBrandIcon, "https://" + ad.getAdvertiserImage(), null);
            advertiserName.setText(ad.getAdvertiserName());
            adTitle.setText(ad.getAdTitle());
            adDescription.setText(ad.getAdDescription());
            ctaButton.setText(ad.getButtonName());

            ctaButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendAdAction(Constants.KEY_CLICKED);
                    dismiss();
                    MobAdUtils.openWebUrlExternal(getActivity(), ad.getButtonLink());
                }
            });

            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();

                }
            });
        }

    }


    public void sendAdAction(String action) {
        apiService = ApiClient.getClient().create(ApiService.class);
        handelErrors = new HandelErrors(getActivity());
        String deviceId = MobAdUtils.getDeviceID(getActivity());
        Log.e(LOG_TAG, " Android ID: " + deviceId);
        adActionCall = apiService.sendAdAction(deviceId, String.valueOf(ad.getAdId()), action);
        adActionCall.enqueue(new Callback<Action>() {
            @Override
            public void onResponse(@NonNull Call<Action> call, @NonNull Response<Action> response) {
                int code = response.code();
                Log.i("Status Code: ", String.valueOf(code));
                if (response.isSuccessful()) {
                    // 200 OK!
                    if (response.body() != null) {
                        Log.i(LOG_TAG, "Result: " + response.body().toString());
                        Action adActionResult = response.body();
                        assert adActionResult != null;
                        if (adActionResult.getStatus().equals("Success")) {
                            setEarnedCoinsMessage(adActionResult.getMessage());
                            earnedCoinsLayout.setVisibility(View.VISIBLE);
                            ViewAnimation.showIn(earnedCoinsLayout);
                            runnable = new Runnable() {
                                @Override
                                public void run() {
                                    ViewAnimation.showOut(earnedCoinsLayout);
                                }
                            };
                            handler.postDelayed(runnable, 3000);
                        }

                    }

                } else {
                    handelErrors.handleStatusCodeErrors(code, adActionCall, LOG_TAG);

                }

            }

            @Override
            public void onFailure(Call<Action> call, Throwable t) {
                handelErrors.onFailureCall(call, t, LOG_TAG);
            }

        });
    }


    private void setEarnedCoinsMessage(String message) {
        earnedCoinsMessage.setText(message);
    }

    private void toastMessageFloatingImage(String message) {
        final Toast toast = new Toast(getActivity());
        toast.setDuration(Toast.LENGTH_SHORT);

        //inflate view
        View customView = getLayoutInflater().inflate(R.layout.earned_coins_toast_floating_image, null);
        TextView messageText = customView.findViewById(R.id.message);
        messageText.setText(message);
        TextView appButton = customView.findViewById(R.id.app_button);
        appButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MobAdUtils.startNewActivity(getActivity(), "com.oqunet.mobad");
            }
        });
        toast.setView(customView);
        toast.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppDatabase.getInstance(getActivity()).getAdDao().deleteAd(ad);
        mobAd.unregisterPhoneCallsReceiver();
        showingAdInterface.onShownAd();
        Log.i(LOG_TAG, "onDestroy: Delete Ad... Unregister Phone Calls Receiver... Finish Display Ad Activity...");
    }

}