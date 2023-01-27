package com.inventory.service.Inventory.Servie.dao;

import com.inventory.service.Inventory.Servie.Entity.Container;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ContainerDao{

    List<Container> getAll();

    int addContainer(Container container);
}
