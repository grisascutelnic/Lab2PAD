package labs.partea1.controllers;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import labs.partea1.MySQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Path("/employees")
public class EmployeeController {

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAll() {
        List<String> list = new ArrayList<>();
        try (Connection conn = MySQLConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM employees")) {

            while (rs.next()) {
                list.add(rs.getInt("id") + ":" + rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok(list).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") int id) {
        try (Connection conn = MySQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM employees WHERE id = ?")) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Response.status(Response.Status.NOT_FOUND).build();
                return Response.ok(rs.getString("name")).build();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response put(@PathParam("id") int id, String name) {
        try (Connection conn = MySQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO employees (id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name=?")) {

            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setString(3, name);
            ps.executeUpdate();
            return Response.ok("Saved").build();

        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
