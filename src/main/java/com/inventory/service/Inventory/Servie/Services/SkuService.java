package com.inventory.service.Inventory.Servie.Services;

import com.inventory.service.Inventory.Servie.Entity.Container;
import com.inventory.service.Inventory.Servie.Entity.SKU;
import com.inventory.service.Inventory.Servie.dao.SkuDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
public class SkuService implements SkuDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<SKU> getAll() {
        String query = "SELECT * FROM sku s Left join sku_containers scl on s.sku_id = scl.sku_id";
        return jdbcTemplate.query(query, new ResultSetExtractor<List<SKU>>() {
            @Override
            public List<SKU> extractData(ResultSet rs) throws SQLException, DataAccessException {
                HashMap<Integer, SKU> map = new HashMap<>();
                while (rs.next()) {
                    SKU sku = map.getOrDefault(rs.getInt("sku_id"), new SKU());
                    sku.setSku_id(rs.getInt("sku_id"));
                    sku.setQuantity(rs.getInt("quantity"));
                    sku.setName(rs.getString("name"));
                    sku.setExpiry_date(rs.getDate("expiry_date"));
                    List<Integer> list = sku.getContainerList();
                    if(rs.getInt("container_id") == 0) list.add(rs.getInt("container_id"));
                    sku.setContainerList(list);
                    map.put(rs.getInt("sku_id"), sku);
                }

                List<SKU> skuList = new ArrayList<>(map.values());
                return skuList;
            }
        });
    }


    @Override
    public void mapContainer(String request, int added_quantity, int added_id, Object object) throws SQLException {
        List<Container> container_list;
        if (request.equals("PUT")) {
            String query1 = "Update sku set quantity = quantity + ? where sku_id = ?";
            jdbcTemplate.update(query1, added_quantity, added_id);
            String query2 = "SELECT * FROM container c LEFT JOIN sku_containers sc ON c.container_id = sc.container_id  WHERE sc.container_id  is not null and sc.sku_id  = ?";
            container_list = jdbcTemplate.query(query2, new Object[]{added_id}, new ResultSetExtractor<List<Container>>() {
                @Override
                public List<Container> extractData(ResultSet rs) throws SQLException, DataAccessException {
                    List<Container> temp_list = new ArrayList<>();
                    int added =  added_quantity;
                    while (rs.next()) {
                        Container container = new Container();
                        container.setContainer_id(rs.getInt("container_id"));
                        container.setMax_limit(rs.getInt("max_limit"));
                        container.setCurrent(rs.getInt("current"));
                        if (added > 0) {
                            int can_add = rs.getInt("max_limit") - rs.getInt("current");
                            if(can_add > 0){
                                container.setCurrent(rs.getInt("current") + added - can_add > 0 ? can_add : added);
                                String temp_query1 = "Update container set current = current + ? where container_id = ?";
                                jdbcTemplate.update(temp_query1, container.getCurrent(), rs.getInt("container_id"));
                                added = (added - can_add) <= 0 ? 0 : added - can_add ;
                            }

                        } else {
                            container.setCurrent(rs.getInt("current"));
                        }

                        temp_list.add(container);
                    }

                    if (added > 0) {
                        while (added > 0) {
                            String query3 = "Insert into container (max_limit, current) Values (:max_limit, :current) returning container_id";
                            MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
                            mapSqlParameterSource.addValue("max_limit", 100);
                            mapSqlParameterSource.addValue("current", added - 100 > 0 ? 100 : added);
                            added = added - 100 > 0 ? added - 100 : 0;
                            int container_id = namedParameterJdbcTemplate.queryForObject(query3, mapSqlParameterSource, Integer.class);
                            String sql = "SELECT * FROM container WHERE container_id = :container_id";
                            MapSqlParameterSource parameters = new MapSqlParameterSource();
                            parameters.addValue("container_id", container_id);
                            Container container = namedParameterJdbcTemplate.query(sql, parameters, new ResultSetExtractor<Container>() {
                                @Override
                                public Container extractData(ResultSet rs) throws SQLException, DataAccessException {
                                    if (rs.next()) {
                                        Container container1 = new Container();
                                        container1.setCurrent(rs.getInt("current"));
                                        container1.setMax_limit(rs.getInt("max_limit"));
                                        container1.setContainer_id(rs.getInt("container_id"));
                                        return container1;
                                    } else return null;
                                }
                            });
                            temp_list.add(container);
                            String insertSQL = "INSERT INTO sku_containers (sku_id, container_id) VALUES (?, ?)";
                            jdbcTemplate.update(insertSQL, new Object[] { added_id, container_id });
                        }
                    }
                    return temp_list;
                }
            });
            String query4 = "SELECT * FROM sku WHERE sku_id = :sku_id";
            MapSqlParameterSource parameters2 = new MapSqlParameterSource();
            parameters2.addValue("sku_id", added_id);
            SKU sku = namedParameterJdbcTemplate.query(query4, parameters2, new ResultSetExtractor<SKU>() {
                @Override
                public SKU extractData(ResultSet rs) throws SQLException, DataAccessException {
                    if (rs.next()) {
                        SKU sku = new SKU();
                        sku.setName(rs.getString("name"));
                        sku.setSku_id(rs.getInt("sku_id"));
                        sku.setQuantity(rs.getInt("quantity") + added_quantity);
                        sku.setExpiry_date(rs.getDate("expiry_date"));
                        return sku;
                    } else return null;
                }
            });
            List<Integer> container_ids = new ArrayList<>();
            for (Container c : container_list) {
                container_ids.add(c.getContainer_id());
            }
            sku.setContainerList(container_ids);
        }
        else if(request.equals("POST")){
            int sku_id = ((SKU) object).getSku_id();
            SKU sku = (SKU) object;
            String query1 = "INSERT INTO sku (sku_id, expiry_date, name, quantity) VALUES (:sku_id, :expiry_date, :name, :quantity)";
            MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
            mapSqlParameterSource.addValue("sku_id", sku.getSku_id());
            mapSqlParameterSource.addValue("expiry_date", sku.getExpiry_date());
            mapSqlParameterSource.addValue("name", sku.getName());
            mapSqlParameterSource.addValue("quantity", sku.getQuantity());

            namedParameterJdbcTemplate.update(query1, mapSqlParameterSource);

            int add = sku.getQuantity();
            List<Container> temp_list = new ArrayList<>();
            while (add > 0) {
                String query3 = "Insert into container (max_limit, current) Values (:max_limit, :current) returning container_id";
                MapSqlParameterSource param = new MapSqlParameterSource();
                param.addValue("max_limit", 100);
                param.addValue("current", add - 100 > 0 ? 100 : add);
                add = add - 100 > 0 ? add - 100 : 0;
                int container_id = namedParameterJdbcTemplate.queryForObject(query3, param, Integer.class);
                String query4 = "SELECT * FROM container WHERE container_id = :container_id";
                MapSqlParameterSource parameters = new MapSqlParameterSource();
                parameters.addValue("container_id", container_id);
                Container container = namedParameterJdbcTemplate.query(query4, parameters, new ResultSetExtractor<Container>() {
                    @Override
                    public Container extractData(ResultSet rs) throws SQLException, DataAccessException {
                        if (rs.next()) {
                            Container container1 = new Container();
                            container1.setCurrent(rs.getInt("current"));
                            container1.setMax_limit(rs.getInt("max_limit"));
                            container1.setContainer_id(rs.getInt("container_id"));
                            return container1;
                        } else return null;
                    }
                });
                temp_list.add(container);
                List<Integer> container_ids1 = new ArrayList<>();
                for (Container c : temp_list) {
                    container_ids1.add(c.getContainer_id());
                }
                String query6 = "SELECT container_id FROM sku_containers WHERE sku_id = ?";
                List<Integer> containerIds = jdbcTemplate.queryForList(query6, new Object[] { sku_id }, Integer.class);

                for(int c_id : container_ids1){
                    String insertSQL = "INSERT INTO sku_containers (sku_id, container_id) VALUES (?, ?)";
                    jdbcTemplate.update(insertSQL, new Object[] { sku_id, container_id });
                }
            }
        }
    }


}
