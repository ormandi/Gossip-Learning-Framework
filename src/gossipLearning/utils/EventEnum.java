package gossipLearning.utils;

public enum EventEnum {
  ConnectionTimeout,
  WakeUp,
  WakeUpAndSendModel,
  WakeUpAndResendModel,
  WakeUpAndSendGradient,
  WakeUpAndFinishEncrypt,
  WakeUpAndFinishDecrypt;
}
