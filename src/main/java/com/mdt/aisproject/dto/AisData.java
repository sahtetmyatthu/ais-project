package com.mdt.aisproject.dto;
import lombok.Data;
@Data
public class AisData {
    private Integer mmsi;
    private Double lat;
    private Double lon;
    private Integer heading;
}