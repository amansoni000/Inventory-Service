package com.inventory.service.Inventory.Servie.Services;

import com.inventory.service.Inventory.Servie.Entity.Container;
import com.inventory.service.Inventory.Servie.dao.ContainerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ContainerService  implements ContainerDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<Container> getAll() {
        String query = "SELECT * FROM container";
        return jdbcTemplate.query(query, new ResultSetExtractor<List<Container>>() {
            @Override
            public List<Container> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<Container> container_list = new ArrayList<>();
                while(rs.next()){
                    Container container = new Container();
//                    container.setContainer_id(rs.getInt("container_id"));
                    container.setMax_limit(rs.getInt("max_limit"));
                    container.setCurrent(rs.getInt("current"));
//                    container.setSku_id(rs.getInt("sku_id"));
                    container_list.add(container);
                }
                return container_list;
            }
        });
    }

    @Override
    public int addContainer(Container container) {
        String query = "Insert into container (max_limit, current) Values (:max_limit, :current)";
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
//        mapSqlParameterSource.addValue("container_id", container.getContainer_id());
        mapSqlParameterSource.addValue("max_limit", container.getMax_limit());
        mapSqlParameterSource.addValue("current", container.getCurrent());
        return namedParameterJdbcTemplate.update(query, mapSqlParameterSource);
    }
}
