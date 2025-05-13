import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminDashboardService } from '../services/admin-dashboard.service';
import { FormsModule } from '@angular/forms';
import { NgbProgressbarModule, NgbPaginationModule } from '@ng-bootstrap/ng-bootstrap';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';
import { NgbAlertModule } from '@ng-bootstrap/ng-bootstrap';
@Component({
  selector: 'app-student-list',
  standalone: true,
  imports: [CommonModule, FormsModule, NgbProgressbarModule, NgbPaginationModule, NgbAlertModule],
  templateUrl: './student-list.component.html',
  styleUrls: ['./student-list.component.scss']
})
export class StudentListComponent implements OnInit {
  students: any[] = [];
  searchTerm: string = '';
  page: number = 1;
  pageSize: number = 10;
  totalPages: number = 0;
  successMessage: string | null = null;
  constructor(private modalService: NgbModal,private dashboardService: AdminDashboardService) {}

  ngOnInit(): void {
    this.loadStudents();
  }

  loadStudents() {
    this.dashboardService.getStudents(this.searchTerm, this.page - 1, this.pageSize).subscribe({
      next: (data) => {
        this.students = data.students || [];
        this.totalPages = data.totalPages;
      },
      error: (err) => {
        console.error('Erreur lors de la récupération des étudiants', err);
      }
    });
  }
  confirmDelete(studentId: number): void {
    const confirmed = window.confirm("Are you sure you want to delete this student?");
    if (confirmed) {
      this.deleteStudent(studentId);
    }
  }
  
  deleteStudent(studentId: number): void {
     this.dashboardService.deleteStudent(studentId).subscribe({
      next: () => {
        this.students = this.students.filter(s => s.id !== studentId);
        setTimeout(() => {
          this.students = this.students.filter(s => s.id !== studentId);
          this.successMessage = 'Student deleted successfully!';
          // Cache le message après 3 secondes
          setTimeout(() => (this.successMessage = null), 3000);
        }, 300);
      },
      error: (err) => {
        console.error("Failed to delete student", err);
      }
    }); 
  }
  openDeleteDialog(student: any) {
    const modalRef = this.modalService.open(ConfirmDialogComponent, {
      backdrop: 'static',
      centered: true,
      size: 'sm',
      windowClass: 'confirm-modal'
    });
  
    modalRef.componentInstance.message = `Are you sure you want to delete ${student.username}?`;
  
    modalRef.result.then((confirmed) => {
      if (confirmed) {
        this.deleteStudent(student.id);
      }
    }).catch(() => {});
  }
  onSearchChange() {
    this.page = 1;
    this.loadStudents();
  }

  onPageChange(newPage: number) {
    this.page = newPage;
    this.loadStudents();
  }
}
