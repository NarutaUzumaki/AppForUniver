package com.grin.appforuniver.fragments.consultations;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.grin.appforuniver.R;
import com.grin.appforuniver.activities.ConsultationActivity;
import com.grin.appforuniver.data.model.consultation.Consultation;
import com.grin.appforuniver.data.service.ConsultationService;
import com.grin.appforuniver.data.tools.AuthManager;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Response;

import static com.grin.appforuniver.utils.Constants.Roles.ROLE_TEACHER;

public abstract class ConsultationListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, ConsultationService.OnRequestConsultationListListener {
    private static final String TAG = "ConsultationListFragment";
    ConsultationService mService;
    private View mRootView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyState;

    private ItemAdapter<Consultation> mItemAdapter;
    private FastAdapter<Consultation> mFastAdapter;
    OnRecyclerViewScrolled onRecyclerViewScrolled;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mService = ConsultationService.getService();
        mRootView = inflater.inflate(R.layout.fragment_consultations_all, container, false);
        mSwipeRefreshLayout = mRootView.findViewById(R.id.swipe_to_refresh_consultations);
        recyclerView = mRootView.findViewById(R.id.fragment_consultations_all_rv);
        progressBar = mRootView.findViewById(R.id.fragment_consultations_all_pb);
        emptyState = mRootView.findViewById(R.id.consultation_all_empty_state_layout);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(mRootView.getContext()));
        recyclerView.setVisibility(View.INVISIBLE);
        mItemAdapter = new ItemAdapter<>();
        mFastAdapter = FastAdapter.with(mItemAdapter);

        mFastAdapter.setOnClickListener((mView, adapter, item, position) -> {
                    Intent intent = new Intent(getContext(), ConsultationActivity.class);
                    intent.putExtra("Consultation", item.getId());
                    startActivity(intent);
                    return false;
                }
        );
        recyclerView.setAdapter(mFastAdapter);
        if (AuthManager.getInstance().getUserRoles().contains(ROLE_TEACHER.toString())) {
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    onRecyclerViewScrolled.onScrolled(recyclerView, dx, dy);
                }
            });
        }
        return mRootView;
    }

    public abstract void getConsultations(ConsultationService.OnRequestConsultationListListener l);

    @Override
    public void onRefresh() {
        getConsultations(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getConsultations(this);
    }

    public interface OnRecyclerViewScrolled {
        void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy);
    }

    @Override
    public void onRequestConsultationListSuccess(Call<List<Consultation>> call, Response<List<Consultation>> response) {
        if (response.isSuccessful()) {
            mItemAdapter.clear();
            if (response.body() != null) {
                mItemAdapter.add(response.body());
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            }
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRequestConsultationListFailed(Call<List<Consultation>> call, Throwable t) {
        Toasty.error(Objects.requireNonNull(getContext()), Objects.requireNonNull(t.getMessage()), Toast.LENGTH_SHORT, true).show();
        progressBar.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
    }
}
