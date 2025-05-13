import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminDashboardService } from '../../services/admin-dashboard.service'; // ajuste le chemin si besoin
import { AdminCounterStateComponent } from './admin-counter-state/admin-counter-state.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule,AdminCounterStateComponent],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
})
export class AdminDashboardComponent implements OnInit {
  overview: any = null;

  constructor(private dashboardService: AdminDashboardService) {}

  ngOnInit(): void {
    this.dashboardService.getAdminDasbord().subscribe({
      next: (data) => {
        this.overview = data;
        console.log("OVERVIEW LOADED", this.overview);
      },
      error: (err) => console.error('Erreur de chargement du dashboard', err)
    });
  }
}
