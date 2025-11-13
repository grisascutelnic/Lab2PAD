package labs.partea1.controllers;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import labs.partea1.MySQLConnector;
import labs.partea1.model.Employee;
import labs.partea1.model.EmployeeList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Path("/employees")
public class EmployeeController {

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAll(@Context HttpHeaders headers) {

        System.out.println("== HEADERS RECEIVED ==");
        headers.getRequestHeaders().forEach((k, v) -> {
            System.out.println(k + ": " + v);
        });
        System.out.println("=======================");

        List<Employee> list = new ArrayList<>();

        try (Connection conn = MySQLConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM employees")) {

            while (rs.next()) {
                list.add(new Employee(rs.getInt("id"), rs.getString("name")));
            }

        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database error: " + e.getMessage())
                    .build();
        }

        return Response.ok(new EmployeeList(list)).build();
    }

    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getById(@PathParam("id") int id) {
        String sql = "SELECT * FROM employees WHERE id = ?";

        try (Connection conn = MySQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("Employee not found")
                            .build();
                }

                Employee e = new Employee(rs.getInt("id"), rs.getString("name"));
                return Response.ok(e).build();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database error: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response put(@PathParam("id") int id, String name) {
        String sql = "INSERT INTO employees (id, name) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE name = ?";

        try (Connection conn = MySQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setString(3, name);
            ps.executeUpdate();
            return Response.ok("Saved").build();

        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database error: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.TEXT_PLAIN)
    public Response create(Employee employee, @Context UriInfo uriInfo) {
        if (employee == null || employee.getName() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Employee name is required")
                    .build();
        }

        String sql = "INSERT INTO employees (id, name) VALUES (?, ?)";

        try (Connection conn = MySQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employee.getId());
            ps.setString(2, employee.getName());
            ps.executeUpdate();

            UriBuilder builder = uriInfo.getAbsolutePathBuilder();
            builder.path(Integer.toString(employee.getId()));
            return Response.created(builder.build())
                    .entity("Created")
                    .build();

        } catch (SQLException e) {
            // Dacă ID-ul există deja, poți întoarce 409 Conflict sau ceva similar
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database error: " + e.getMessage())
                    .build();
        }
    }
}
