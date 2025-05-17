import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbPaginationModule, NgbProgressbarModule, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AdminDashboardService } from '../services/admin-dashboard.service';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';
import { NgbAlertModule } from '@ng-bootstrap/ng-bootstrap';

@Component({ 
  selector: 'app-instructor-list',
  standalone: true,
  templateUrl: './instructor-list.component.html',
  styleUrls: ['./instructor-list.component.scss'],
  imports: [CommonModule, FormsModule, NgbPaginationModule, NgbProgressbarModule,NgbAlertModule]
})
export class InstructorListComponent implements OnInit {
  instructors: any[] = [];
  searchTerm: string = '';
  page: number = 1;
  pageSize: number = 10;
  totalPages: number = 0;
  successMessage: string | null = null;

  constructor(private dashboardService: AdminDashboardService, private modalService: NgbModal) {}

  ngOnInit(): void {
    this.loadInstructors();
  }

  loadInstructors() {
    this.dashboardService.getInstructors(this.searchTerm, this.page - 1, this.pageSize).subscribe({
      next: (data) => {
        this.instructors = data.instructors || [];
        this.totalPages = data.totalPages;
      },
      error: (err) => {
        console.error('Erreur lors de la récupération des instructeurs', err);
      }
    });
  }

  onSearchChange() {
    this.page = 1;
    this.loadInstructors();
  }

  onPageChange(newPage: number) {
    this.page = newPage;
    this.loadInstructors();
  }

  openDeleteDialog(instructor: any) {
    const modalRef = this.modalService.open(ConfirmDialogComponent, {
      backdrop: 'static',
      centered: true,
      size: 'sm',
      windowClass: 'confirm-modal'
    });
    modalRef.componentInstance.message = `Are you sure you want to delete ${instructor.username}?`;

    modalRef.result.then((confirmed) => {
      if (confirmed) {
        this.deleteInstructor(instructor.id);
      }
    }).catch(() => {});
  }

  deleteInstructor(instructorId: number): void {
    this.dashboardService.deleteStudent(instructorId).subscribe({
      next: () => {
        this.instructors = this.instructors.filter(s => s.id !== instructorId);
        setTimeout(() => {
          this.instructors = this.instructors.filter(s => s.id !== instructorId);
          this.successMessage = 'Instructor deleted successfully';
          setTimeout(() => (this.successMessage = null), 3000);
        }, 300);
      },
      error: (err) => {
        console.error("Failed to delete Instructor", err);
      }
    });   }
}
