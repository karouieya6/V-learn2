import { Routes } from '@angular/router';
import { InstructorDashboardComponent } from './dashboard/instructor-dashboard/instructor-dashboard.component';
import { AccountLayoutComponent } from '../../layout1/account-layout/account-layout.component'; // ou crée AdminLayoutComponent si tu veux un layout différent

export const INSTRUCTOR_ROUTES: Routes = [
  {
    path: '',
    component: AccountLayoutComponent, // ou InstructorLayout si tu en as un
    children: [
      { path: 'dashboard', component: InstructorDashboardComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
      path: 'courses',
      loadComponent: () => import('./course-list/instructor-course-list.component').then(m => m.InstructorCourseListComponent)
},
 {
        path: 'profile',
        loadComponent: () => import('../admin/admin-profile/admin-profile.component').then(m => m.AdminProfileComponent)
      }, 
    ]
  }
];
