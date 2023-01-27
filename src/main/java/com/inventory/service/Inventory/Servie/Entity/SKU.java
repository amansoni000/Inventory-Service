package com.inventory.service.Inventory.Servie.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SKU {
    @Id
    private int sku_id;
    private String name;
    private int quantity;
    private Date expiry_date;

    @Column(name = "containers")
    @ElementCollection(targetClass = Integer.class)
    private List<Integer> containerList;

}
