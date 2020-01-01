package com.grin.appforuniver.fragments.consultations;

import com.grin.appforuniver.data.WebServices.ConsultationInterface;
import com.grin.appforuniver.data.model.consultation.Consultation;

import java.util.List;

import retrofit2.Call;

public class ConsultationsAllFragment extends ConsultationListFragment {

    public ConsultationsAllFragment(OnRecyclerViewScrolled onRecyclerViewScrolled) {
        this.onRecyclerViewScrolled = onRecyclerViewScrolled;
    }

    @Override
    public Call<List<Consultation>> getWhatConsultations(ConsultationInterface consultationInterface) {
        return consultationInterface.getAllConsultations();
    }
}