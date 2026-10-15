package com.coinis.splitter.api.dto;

public record InvitationResponse(long id, long eventId, String eventName, String invitedBy, String invitee) {
}
