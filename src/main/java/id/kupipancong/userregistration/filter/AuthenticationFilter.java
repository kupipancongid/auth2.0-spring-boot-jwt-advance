package id.kupipancong.userregistration.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        if (!request.getRequestURI().contains("/api/") && !request.getRequestURI().contains("/assets/")){
//            if (!request.getRequestURI().equals("/auth")
//                    && !request.getRequestURI().equals("/")){
//                User user = authenticationService.getUserFromToken(request);
//                if (user==null){
//                    authenticationService.removeCookie(response, "access_token");
//                    authenticationService.removeCookie(response, "refresh_token");
//                    response.sendRedirect("/auth");
//                }else{
//                    ArrayList<ProgramUserResponse> programUserResponses = new ArrayList<ProgramUserResponse>(authenticationService.getProgramUser(user));
//                    Collections.sort(programUserResponses, (ProgramUserResponse o1, ProgramUserResponse o2) -> o1.getFeatureId().compareTo(o2.getFeatureId()));
//
//                    request.setAttribute("program_users", programUserResponses);
//                    List<String> coreWhiteList = List.of(
//                            "/dashboard",
//                            "/account/",
//                            "/account/security",
//                            "/account/profile",
//                            "/account/log",
//                            "/account/access-list"
//                    );
//                    if (!coreWhiteList.contains(request.getRequestURI())){
//                        if (!containsUrl(programUserResponses, request.getRequestURI())){
//                            response.sendRedirect("/dashboard");
//                        }
//                    }
//                }
//            }
//        }

        filterChain.doFilter(request, response);
    }
}
