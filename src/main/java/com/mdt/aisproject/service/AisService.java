package com.mdt.aisproject.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.mdt.aisproject.dto.AisData;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisPositionMessage;
import dk.dma.ais.sentence.Vdm;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Getter
@RequiredArgsConstructor
public class AisService {

    private final AisData latestData = new AisData();
    private final SocketIOServer socketIOServer; // Injected SocketIOServer

    public void handleNmeaSentence(String sentence, String sourceIp) {
        System.out.println("Received NMEA sentence: " + sentence);

        try {
            // Ensure sentence has valid checksum
            String sentenceWithChecksum = ensureValidChecksum(sentence);
            Vdm vdm = new Vdm();
            int result = vdm.parse(sentenceWithChecksum);
            if (result == 0) {
                AisMessage aisMessage = AisMessage.getInstance(vdm);
                processAisMessage(aisMessage, sourceIp);
            } else {
                System.out.println("[" + sourceIp + "] Invalid NMEA sentence parse result: " + sentence);
            }
        } catch (Exception e) {
            System.out.println("[" + sourceIp + "] Failed to parse NMEA sentence: " + e.getMessage());
        }
    }

    private void processAisMessage(AisMessage message, String sourceIp) {
        System.out.println("[" + sourceIp + "] Decoded AIS message: " + message);

        if (message instanceof AisPositionMessage posMessage) {
            latestData.setMmsi(Integer.valueOf(String.valueOf(posMessage.getUserId())));
            latestData.setLat(posMessage.getPos().getRawLatitude() / 600_000.0);
            latestData.setLon(posMessage.getPos().getRawLongitude() / 600_000.0);
            latestData.setHeading(posMessage.getTrueHeading() >= 0 ? posMessage.getTrueHeading() : null);

            System.out.println("[" + sourceIp + "] Decoded AIS Data: MMSI=" + latestData.getMmsi() +
                    ", Lat=" + latestData.getLat() +
                    ", Lon=" + latestData.getLon() +
                    ", Heading=" + (latestData.getHeading() != null ? latestData.getHeading() : "N/A"));

            socketIOServer.getBroadcastOperations().sendEvent("update", latestData);
        }
    }

    private String ensureValidChecksum(String sentence) {
        if (sentence == null || !sentence.startsWith("!")) {
            System.out.println("Invalid NMEA sentence format: " + sentence);
            return sentence; // Return as-is if invalid
        }

        // Split sentence at '*' to check for existing checksum
        int asteriskIndex = sentence.indexOf('*');
        String sentenceWithoutChecksum = asteriskIndex >= 0 ? sentence.substring(0, asteriskIndex) : sentence;
        String calculatedChecksum = calculateNmeaChecksum(sentenceWithoutChecksum);

        // Append or replace checksum
        return asteriskIndex >= 0 ? sentenceWithoutChecksum + "*" + calculatedChecksum : sentence + "*" + calculatedChecksum;
    }

    private String calculateNmeaChecksum(String sentence) {
        int checksum = 0;
        for (int i = 1; i < sentence.length() && sentence.charAt(i) != '*'; i++) {
            checksum ^= sentence.charAt(i);
        }
        return String.format("%02X", checksum);
    }
}