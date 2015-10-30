/*******************************************************************************
 * Copyright 2015 SPECURE GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package at.alladin.rmbt.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import at.alladin.rmbt.db.TestStat;

public class TestStatDao implements CrudPrimaryKeyDao<TestStat, Long> {

	private final Connection conn;
	
	/**
	 * 
	 * @param conn
	 */
	public TestStatDao(Connection conn) {
		this.conn = conn;
	}

	@Override
	public TestStat getById(Long id) throws SQLException {
		try (PreparedStatement psGetById = conn.prepareStatement("SELECT test_uid, cpu_usage, mem_usage FROM test_stat WHERE test_uid = ?"))
		{
    		psGetById.setLong(1, id);
    		
    		if (psGetById.execute()) {
    			try (ResultSet rs = psGetById.getResultSet())
    			{
        			if (rs.next()) {
        				final TestStat ts = instantiateItem(rs);
        				return ts;
        			}
    			} catch (JSONException e) {
					e.printStackTrace();
					return null;
				}
    		}

    		return null;
		}
	}

	@Override
	public List<TestStat> getAll() throws SQLException {
		final List<TestStat> resultList = new ArrayList<>();
		try (PreparedStatement psGetAll = conn.prepareStatement("SELECT test_uid, cpu_usage, mem_usage FROM test_stat WHERE test_uid = ?"))
		{
    		
    		if (psGetAll.execute()) {
    			try (ResultSet rs = psGetAll.getResultSet())
    			{
        			while (rs.next()) {
        				resultList.add(instantiateItem(rs));
        			}
        			
        			return resultList;
    			} catch (JSONException e) {
					e.printStackTrace();
				}
    		}
		}
		
		return null;
	}

	@Override
	public int delete(TestStat entity) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int save(TestStat result) throws SQLException {		
		final String sql = "INSERT INTO test_stat (test_uid, cpu_usage, mem_usage) VALUES (?,?::json,?::json)";
		final PreparedStatement ps = conn.prepareStatement(sql);
		ps.setLong(1, result.getTestUid());
		ps.setString(2, result.getCpuUsage().toString());
		ps.setString(3, result.getMemUsage().toString());
		return ps.executeUpdate();
	}

	@Override
	public int update(TestStat entity) throws SQLException {
		final String sql = "UPDATE test_stat SET cpu_usage = ?::json, mem_usage = ?::json, WHERE test_uid = ?";
		final PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, entity.getCpuUsage().toString());
		ps.setString(2, entity.getMemUsage().toString());
		ps.setLong(3, entity.getTestUid());
		return ps.executeUpdate();
	}

	/**
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 * @throws JSONException
	 */
	private static TestStat instantiateItem(ResultSet rs) throws SQLException, JSONException {
		TestStat result = new TestStat();
		result.setTestUid(rs.getLong("test_uid"));
		result.setCpuUsage(new JSONObject(rs.getString("cpu_usage")));
		result.setMemUsage(new JSONObject(rs.getString("mem_usage")));
		return result;
	}
}
