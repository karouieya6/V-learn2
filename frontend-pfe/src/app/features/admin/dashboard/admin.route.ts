import { Routes } from '@angular/router';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { AccountLayoutComponent } from '../../../layout1/account-layout/account-layout.component'; // ou crée AdminLayoutComponent si tu veux un layout différent

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    component: AccountLayoutComponent,
    children: [
      {
        path: 'users/edit/:id',
        loadComponent: () => import('../user-edit/user-edit.component').then(m => m.UserEditComponent)
      }, 
      { path: 'dashboard', component: AdminDashboardComponent },
      {
        path: 'instructors/list',
        loadComponent: () => import('../instructor-list/instructor-list.component').then(m => m.InstructorListComponent)
      },
      {
        path: 'students',
        loadComponent: () => import('../student-list/student-list.component').then(m => m.StudentListComponent)
      },
      {
        path: 'instructors/requests',
        loadComponent: () => import('../instructor-request-list/instructor-request-list.component').then(m => m.InstructorRequestListComponent)
      },     
         
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  }
];
