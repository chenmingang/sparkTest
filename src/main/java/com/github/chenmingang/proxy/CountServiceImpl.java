package com.github.chenmingang.proxy;

public class CountServiceImpl implements CountService {
  private int count = 0;
  @Override
  public int count() {
    return count ++;
  }
}