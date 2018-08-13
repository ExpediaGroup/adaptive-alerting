package com.expedia.adaptivealerting.dataservice;

import com.expedia.adaptivealerting.core.data.Mpoint;

import java.util.List;

public interface DataSinkService {
  void put(List<Mpoint> records);
}
