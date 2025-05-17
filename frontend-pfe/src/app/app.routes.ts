import { RoleGuard } from './services/role.guard';
import { Routes } from '@angular/router';
import { MainLayoutComponent } from './layout1/mainlayoutcomponent/mainlayout.component';
import { HomeThreeComponent } from './pages/Home/home-three-main/home-three.component';
import { DashboardComponent } from './features/student/dashboard/dashboard.component';
export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home', component: HomeThreeComponent },
      {
        path: 'dashboard',
        component: DashboardComponent,
        canActivate: [RoleGuard],
        data: { roles: ['ADMIN', 'INSTRUCTOR', 'STUDENT'] } // all allowed
      }
    ]
  },

  {
    path: 'student',
    loadChildren: () =>
      import('./features/student/dashboard/student.route').then(m => m.STUDENT_ROUTES)

  },
  {
    path: 'admin',
    loadChildren: () =>
      import('./features/admin/dashboard/admin.route').then(m => m.ADMIN_ROUTES),
    canActivate: [RoleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
  path: 'instructor',
  loadChildren: () =>
    import('./features/instructor/instructor.route').then(m => m.INSTRUCTOR_ROUTES),
  canActivate: [RoleGuard],
  data: { roles: ['INSTRUCTOR'] }
},

 

  // auth routes
  { path: 'sign-up', loadComponent: () => import('./auth/sign-up/sign-up-main/sign-up-main.component').then(m => m.SignUpMainComponent) },
  { path: 'sign-in', loadComponent: () => import('./auth/sign-in/sign-in-main/sign-in-main.component').then(m => m.SignInMainComponent) },
  { path: 'forgot-password', loadComponent: () => import('./auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent) },
  { path: 'reset-password', loadComponent: () => import('./auth/reset-password/reset-password.component').then(m => m.ResetPasswordComponent) },

  { path: '**', redirectTo: 'home' }
];
