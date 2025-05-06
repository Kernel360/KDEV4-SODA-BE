package com.soda.project.domain.company;

import com.soda.member.entity.Company;
import com.soda.project.domain.Project;
import org.springframework.stereotype.Component;

@Component
public class CompanyProjectFactory {

    public CompanyProject createClientCompany(Company company, Project project) {
        return CompanyProject.createClientCompany(company, project);
    }

    public CompanyProject createDevCompany(Company company, Project project) {
        return CompanyProject.createDevCompany(company, project);
    }
}
