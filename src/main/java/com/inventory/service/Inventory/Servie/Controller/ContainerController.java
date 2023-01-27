package com.inventory.service.Inventory.Servie.Controller;

import com.inventory.service.Inventory.Servie.Entity.Container;
import com.inventory.service.Inventory.Servie.dao.ContainerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//@RequestMapping("containers")
public class ContainerController {

    @Autowired
    private ContainerDao containerDao;

    @GetMapping("containers")
    private List<Container> getAllContainers(){
        return containerDao.getAll();
    }

    @PostMapping("containers")
    public int addContainer(@RequestBody Container container){
        return containerDao.addContainer(container);
    }
}
