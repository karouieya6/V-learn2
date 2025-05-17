import { Component } from '@angular/core';
import { InstructorDashboardService } from '../../services/instructor-dashboard.service';
import { CommonModule } from '@angular/common';
import { InstructorCounterStateComponent } from '../components/instructor-counter-state/instructor-counter-state.component';

@Component({
  selector: 'app-instructor-dashboard',
  imports: [CommonModule,InstructorCounterStateComponent],
  templateUrl: './instructor-dashboard.component.html',
  styleUrl: './instructor-dashboard.component.scss'
})
export class InstructorDashboardComponent {
 overview: any = null;

  constructor(private dashboardService: InstructorDashboardService) {}
ngOnInit(): void {
    this.dashboardService.getOverview().subscribe({
      next: (data) => this.overview = data,
      error: (err) => console.error('Error loading instructor dashboard', err)
    });
  }
}
