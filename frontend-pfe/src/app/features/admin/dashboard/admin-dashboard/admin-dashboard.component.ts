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
  downloadExcel() {
    this.dashboardService.downloadDashboardExcel().subscribe({
      next: (data: Blob) => {
        const blob = new Blob([data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'course_engagement_report.xlsx';
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error('Download failed', err);
        alert('❌ Error downloading Excel report');
      }
    });
  }
}
