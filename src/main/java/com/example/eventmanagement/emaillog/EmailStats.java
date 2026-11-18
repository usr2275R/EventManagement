package com.example.eventmanagement.emaillog;

public record EmailStats(long total, long send, long failed, Double avgDuration) {
}
