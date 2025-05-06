import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard.component';
import { CourseListComponent } from '../course-list/course-list.component';
import { CourseResumeComponent } from '../course-resume/course-resume.component';
import { EditProfileComponent } from '../../../shared/edit-profile/edit-profile.component';
import { AccountLayoutComponent } from '../../../layout1/account-layout/account-layout.component';
export const STUDENT_ROUTES: Routes = [
  {
    path: '',
    component: AccountLayoutComponent,
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'course-list', component: CourseListComponent },
      { path: 'course-resume', component: CourseResumeComponent },
      { path: 'edit-profile', component: EditProfileComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  }
];