package com.grin.appforuniver.fragments.schedule;

import com.grin.appforuniver.data.WebServices.ScheduleInterface;
import com.grin.appforuniver.data.WebServices.ServiceGenerator;
import com.grin.appforuniver.data.model.schedule.Classes;
import com.grin.appforuniver.data.model.schedule.Groups;
import com.grin.appforuniver.data.model.schedule.Professors;
import com.grin.appforuniver.data.model.schedule.Rooms;
import com.grin.appforuniver.data.utils.PreferenceUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

import static com.grin.appforuniver.data.utils.Constants.Place;
import static com.grin.appforuniver.data.utils.Constants.Roles.ROLE_TEACHER;
import static com.grin.appforuniver.data.utils.Constants.TypesOfClasses;
import static com.grin.appforuniver.data.utils.Constants.Week;

public class ScheduleFiltrationManager {
    private String subject;
    private String type;
    private int professorId;
    private int roomId;
    private int groupId;
    private String place;
    private String week;
    private Callback<List<Classes>> callbackRetrofitSchedule;

    public ScheduleFiltrationManager(Callback<List<Classes>> callbackRetrofitSchedule) {
        this.callbackRetrofitSchedule = callbackRetrofitSchedule;
    }

    private void initializeParameters(Classes subject, TypesOfClasses type, Professors professor, Rooms room, Groups group, Place place, Week week) {
        this.subject = (subject != null) ? subject.getSubject() : null;
        this.type = (type != null) ? type.toString() : null;
        this.professorId = (professor != null) ? professor.getId() : -1;
        this.roomId = (room != null) ? room.getId() : -1;
        this.groupId = (group != null) ? group.getmId() : -1;
        this.place = (place != null) ? place.toString() : null;
        this.week = (week != null) ? week.toString() : null;
    }

    public void getSchedule(Classes subject, TypesOfClasses type, Professors professor, Rooms room, Groups group, Place place, Week week) {
        initializeParameters(subject, type, professor, room, group, place, week);

        ScheduleInterface scheduleInterface = ServiceGenerator.createService(ScheduleInterface.class);
        Call<List<Classes>> call;

        if (this.subject == null & this.type == null & this.professorId == -1 &
                this.roomId == -1 & this.groupId == -1 & this.place == null &
                this.week == null) {
            call = scheduleInterface.getScheduleCurrentUser();
        } else {
            call = scheduleInterface.getScheduleByCriteria(
                    this.subject,
                    this.type,
                    this.professorId,
                    this.roomId,
                    this.groupId,
                    this.place,
                    this.week, -1, null);
        }
        if (callbackRetrofitSchedule != null)
            call.enqueue(callbackRetrofitSchedule);
    }

    public boolean isProfessorsSchedule() {
        boolean isProfessorSearched = professorId != -1 & groupId == -1;
        return PreferenceUtils.getUserRoles().contains(ROLE_TEACHER.toString()) | isProfessorSearched;
    }
}