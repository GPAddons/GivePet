package com.github.gpaddons.givepet.lang;

import com.github.gpaddons.util.lang.value.ConfigMessage;
import org.jetbrains.annotations.NotNull;

public enum Messages implements ConfigMessage {

  SEND_PENDING_FROM("send.pending.from", "&cYou already have a pending transfer to &b$recipientName!&c Please wait for them to respond."),
  SEND_PENDING_TO("send.pending.to", "&b$recipientName&a already has a pending pet transfer! Please wait for them to respond."),
  SEND_TARGET_TAMEABLE("send.target_tameable", "&cYou must target a creature you have tamed to transfer!"),
  SEND_NO_RECIPIENT("send.no_recipient", "&cInvalid recipient!"),
  SEND_OFFER("send.offer", "&a$ownerName would like to transfer &b[$tamed]&a to you!\nYou have two minutes to &b[$acceptpet]&a or &b[$declinepet]&a."),
  SEND_OFFERED("send.offered", "&aOffered &b[$tamed]&a to &b$recipientName&a! They have two minutes to respond."),

  RECEIVE_NO_PENDING("receive.no_pending", "&cYou don't have any pending pet transfers!"),
  RECEIVE_NOT_FOUND_SENDER("receive.not_found.sender", "&cUnable to transfer pet! Please make sure you stay near it until the transfer completes."),
  RECEIVE_NOT_FOUND_RECIPIENT("receive.not_found.recipient", "&cUnable to locate pet!"),
  RECEIVE_ACCEPT_SENDER("receive.accept.sender", "&aPet transferred!"),
  RECEIVE_ACCEPT_RECIPIENT("receive.accept.recipient", "&aPet transferred!"),
  RECEIVE_DECLINE_SENDER("receive.decline.sender", "&cPet transfer declined!"),
  RECEIVE_DECLINE_RECIPIENT("receive.decline.recipient", "&aDeclined pet transfer from &b$ownerName&a!\nIf they continue to send you unwanted pets, you can &b[$/ignore]&a them.");

  private final String key;
  private final String defaultValue;

  Messages(String key, String defaultValue) {
    this.key = key;
    this.defaultValue = defaultValue;
  }

  @Override
  public @NotNull String getKey() {
    return key;
  }

  @Override
  public @NotNull String getDefault() {
    return defaultValue;
  }

}
