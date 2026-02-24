import { Component, OnInit } from '@angular/core';
import { UserService } from '../services/user.service';
import User from '../models/user.model';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html'
})
export class RegisterComponent implements OnInit {

  users: User[] = [];

  user: User = {
    name: '',
    email: '',
    password: ''
  };

  editingId?: number;

  constructor(private service: UserService) {}

  ngOnInit(): void {
    this.loadUsers();
  }


getUsers() {
  this.service.getUsers().subscribe(data => {
    this.users = data;
  });
}


  loadUsers() {
    this.service.getUsers().subscribe(data => {
      this.users = data;
    });
  }

  saveUser() {
    if (this.editingId) {
      this.service.updateUser(this.editingId, this.user)
        .subscribe(() => {
          this.resetForm();
          this.loadUsers();
        });
    } else {
      this.service.createUser(this.user)
        .subscribe(() => {
          this.resetForm();
          this.loadUsers();
        });
    }
  }

  editUser(user: User) {
    this.user = { ...user };
    this.editingId = user.id;
  }

  deleteUser(id: number) {
    this.service.deleteUser(id)
      .subscribe(() => this.loadUsers());
  }

  resetForm() {
    this.user = { name: '', email: '', password: '' };
    this.editingId = undefined;
  }
}

