package com.expedia.adaptivealerting.modelservice.detectormapper.service.es;
import com.expedia.adaptivealerting.modelservice.detectormapper.service.Detector;
import com.expedia.adaptivealerting.modelservice.detectormapper.service.User;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DetectorMappingEntity {
    // Prefixing variable names with 'aa_' to reserve these fields to be used in ES mappings.
    public static final String AA_PREFIX = "aa_";
    public static final String USER_KEYWORD = AA_PREFIX + "user";
    public static final String USER_ID_KEYWORD = "id";
    public static final String DETECTOR_KEYWORD = AA_PREFIX + "detector";
    public static final String DETECTOR_ID_KEYWORD = "id";
    public static final String QUERY_KEYWORD = AA_PREFIX + "query";
    public static final String ENABLED = AA_PREFIX + "enabled";
    public static final String LAST_MOD_TIME_KEYWORD = AA_PREFIX + "lastModifiedTime";
    public static final String CREATE_TIME_KEYWORD = AA_PREFIX + "createdTime";

    @SerializedName(USER_KEYWORD)
    private User user;
    @SerializedName(DETECTOR_KEYWORD)
    private Detector detector;
    @SerializedName(QUERY_KEYWORD)
    private Query query;
    @SerializedName(ENABLED)
    private boolean enabled;
    @SerializedName(LAST_MOD_TIME_KEYWORD)
    private long lastModifiedTimeInMillis;
    @SerializedName(CREATE_TIME_KEYWORD)
    private long createdTimeInMillis;
}
